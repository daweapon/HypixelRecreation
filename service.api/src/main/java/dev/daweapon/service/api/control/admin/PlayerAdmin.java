package dev.daweapon.service.api.control.admin;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.data.NameIndex;
import dev.daweapon.commons.data.ProfileIndexes;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.objects.admin.AdminPlayerActionProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminPlayerActionProtocol.ActionRequest;
import dev.daweapon.commons.protocol.objects.admin.AdminPlayerActionProtocol.ActionResponse;
import dev.daweapon.commons.protocol.objects.presence.GetPresenceBulkProtocol;
import dev.daweapon.commons.protocol.objects.punishment.PunishPlayerServiceProtocol;
import dev.daweapon.commons.protocol.objects.punishment.UnpunishPlayerProtocol;
import dev.daweapon.commons.punishment.ActivePunishment;
import dev.daweapon.commons.punishment.PunishmentReason;
import dev.daweapon.commons.punishment.PunishmentRedis;
import dev.daweapon.commons.punishment.PunishmentType;
import dev.daweapon.commons.punishment.template.BanType;
import dev.daweapon.commons.punishment.template.MuteType;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.skyblock.CoopStorage;
import dev.daweapon.commons.skyblock.SkyBlockProfileFields;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.PlayerWipeService;
import dev.daweapon.type.generic.data.WipeSnapshotService;
import dev.daweapon.type.generic.user.categories.Rank;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Player admin from the control app. Wipe and restore rewrite whole Redis documents, which a live
// session would overwrite on its next save, so both refuse while the player is online anywhere.
public final class PlayerAdmin {
    public record ProfileSummary(String id, @Nullable String name, boolean selected, double coins,
                                 @Nullable String coopId, int coopMembers) {}

    public record PunishmentSummary(String type, String id, String reason, long expiresAt) {}

    public record Lookup(UUID uuid, String name, String rank, boolean staff, @Nullable String onlineServer,
                         @Nullable String onlineServerType, @Nullable Long lastSeen, List<ProfileSummary> profiles,
                         List<PunishmentSummary> punishments, @Nullable Long wipeSnapshotAt,
                         @Nullable Long restorableUntil) {}

    public record Options(List<String> ranks, List<String> banReasons, List<String> muteReasons) {}

    public record Outcome(boolean ok, String detail) {}

    private static final JacksonSerializer<Rank> RANK_SERIALIZER = new JacksonSerializer<>(Rank.class);
    private static final AdminPlayerActionProtocol ACTION_PROTOCOL = new AdminPlayerActionProtocol();
    private static final int ACTION_TIMEOUT_MS = 2500;

    private PlayerAdmin() {}

    public static @Nullable UUID resolve(String query) {
        if (query == null || query.isBlank()) return null;
        try {
            return UUID.fromString(query.trim());
        } catch (IllegalArgumentException ignored) {
            return NameIndex.lookup(query.trim());
        }
    }

    public static Options options() {
        return new Options(Arrays.stream(Rank.values()).map(Enum::name).toList(),
                Arrays.stream(BanType.values()).map(Enum::name).toList(),
                Arrays.stream(MuteType.values()).map(Enum::name).toList());
    }

    public static Lookup lookup(UUID uuid) throws Exception {
        AdminBackends.ensureReady();
        Map<String, Object> account;
        List<ProfileSummary> profiles = new ArrayList<>();
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            account = OrphanScan.readMap(jedis, OrphanScan.ACCOUNT_PREFIX + uuid);
            String skyblockIndex = OrphanScan.unquote(account.get(ProfileIndexes.SKYBLOCK.fullKey()));
            UUID selected = ProfileIndexes.decodeSelected(skyblockIndex);
            for (UUID profileId : PlayerWipeService.profileIdsOf(uuid)) {
                profiles.add(profileSummary(jedis, profileId, profileId.equals(selected)));
            }
        } finally {
            release(uuid);
        }

        Rank rank = parseRank(OrphanScan.unquote(account.get(HypixelDataHandler.Data.RANK.accountField().fullKey())));
        String name = OrphanScan.unquote(account.get(HypixelDataHandler.Data.IGN.accountField().fullKey()));

        Optional<FleetStatus.Located> online = FleetStatus.locate(uuid);
        List<PunishmentSummary> punishments = PunishmentRedis.getAllActive(uuid).stream()
                .map(PlayerAdmin::summarise).toList();
        Long snapshotAt = wipeSnapshotAt(uuid);

        return new Lookup(uuid, name == null ? uuid.toString() : name, rank.name(), rank.isStaff(),
                online.map(located -> located.server().status().serverName()).orElse(null),
                online.map(located -> located.server().status().serverType()).orElse(null),
                lastSeen(uuid), profiles, punishments, snapshotAt,
                snapshotAt == null ? null : snapshotAt + WipeSnapshotService.RESTORE_WINDOW_MILLIS);
    }

    public static Outcome setRank(UUID uuid, String rankName) throws Exception {
        Rank rank = Rank.valueOf(rankName);
        Optional<ActionResponse> live = actOnline(uuid, AdminPlayerActionProtocol.SET_RANK, rank.name());
        if (live.isPresent()) return new Outcome(!live.get().detail().startsWith("failed"), live.get().detail());

        try {
            SharedData.account().set(uuid, HypixelDataHandler.Data.RANK.accountField(), RANK_SERIALIZER.serialize(rank));
        } finally {
            release(uuid);
        }
        return new Outcome(true, "rank set to " + rank.name() + " in stored account data, applies on next login");
    }

    public static Outcome kick(UUID uuid, String reason) throws Exception {
        return actOnline(uuid, AdminPlayerActionProtocol.KICK, reason)
                .map(response -> new Outcome(!response.detail().startsWith("failed"), response.detail()))
                .orElseGet(() -> new Outcome(false, "player is not online on any server"));
    }

    public static Outcome punish(UUID uuid, String type, String reasonName, long durationMillis, UUID staff) throws Exception {
        PunishmentReason reason = type.equals(PunishmentType.BAN.name())
                ? new PunishmentReason(BanType.valueOf(reasonName))
                : new PunishmentReason(MuteType.valueOf(reasonName));
        long expiresAt = durationMillis > 0 ? System.currentTimeMillis() + durationMillis : -1;
        PunishPlayerServiceProtocol.PunishPlayerResponse response = RedisClient.requestService(ServiceType.PUNISHMENT,
                new PunishPlayerServiceProtocol(), new PunishPlayerServiceProtocol.PunishPlayerMessage(
                        uuid, type, reason, staff, new ArrayList<>(), expiresAt)).get(6, TimeUnit.SECONDS);
        if (response.success()) return new Outcome(true, type.toLowerCase() + " issued, id " + response.punishmentId());
        return new Outcome(false, response.errorCode() + (response.errorMessage() == null ? "" : ": " + response.errorMessage()));
    }

    public static Outcome unpunish(UUID uuid, String type, UUID staff) throws Exception {
        UnpunishPlayerProtocol.UnpunishPlayerResponse response = RedisClient.requestService(ServiceType.PUNISHMENT,
                new UnpunishPlayerProtocol(), new UnpunishPlayerProtocol.UnpunishPlayerMessage(uuid, staff, type))
                .get(6, TimeUnit.SECONDS);
        return response.success() ? new Outcome(true, type.toLowerCase() + " lifted")
                : new Outcome(false, String.valueOf(response.errorMessage()));
    }

    public static Outcome wipe(UUID uuid) throws Exception {
        Optional<FleetStatus.Located> online = FleetStatus.locate(uuid);
        if (online.isPresent()) return refuseOnline(online.get());
        if (!PlayerWipeService.begin(uuid)) return new Outcome(false, "a wipe of this player is already running");

        AdminBackends.ensureReady();
        try {
            WipeSnapshotService.capture(uuid, PlayerWipeService.profileIdsOf(uuid));
            PlayerWipeService.Result result = PlayerWipeService.wipe(uuid);
            return new Outcome(true, "wiped " + result.profiles().size() + " profile(s), " + result.islands()
                    + " island(s), " + result.leaderboards() + " leaderboard entr(ies); restorable for 1 hour");
        } finally {
            release(uuid);
            PlayerWipeService.finish(uuid);
        }
    }

    public static Outcome restore(UUID uuid) throws Exception {
        Optional<FleetStatus.Located> online = FleetStatus.locate(uuid);
        if (online.isPresent()) return refuseOnline(online.get());

        AdminBackends.ensureReady();
        return switch (WipeSnapshotService.restore(uuid)) {
            case SUCCESS -> new Outcome(true, "restored from the wipe snapshot");
            case EXPIRED -> new Outcome(false, "the wipe was more than an hour ago and can no longer be restored");
            case NOT_FOUND -> new Outcome(false, "no recoverable wipe for this player");
        };
    }

    private static Outcome refuseOnline(FleetStatus.Located located) {
        return new Outcome(false, "player is online on " + located.server().status().serverName()
                + "; kick them first and retry once they have left");
    }

    private static Optional<ActionResponse> actOnline(UUID uuid, String action, @Nullable String value) throws Exception {
        Map<UUID, ActionResponse> responses = FleetStatus.broadcast(ACTION_PROTOCOL,
                new ActionRequest(uuid, action, value), ACTION_TIMEOUT_MS);
        return responses.values().stream().filter(response -> response != null && response.handled()).findFirst();
    }

    private static ProfileSummary profileSummary(Jedis jedis, UUID profileId, boolean selected) {
        Map<String, Object> stored = OrphanScan.readMap(jedis, OrphanScan.PROFILE_PREFIX + profileId);
        String name = null;
        double coins = 0;
        Object documentJson = stored.get(SkyBlockProfileFields.DOCUMENT.fullKey());
        if (documentJson != null) {
            try {
                Document document = Document.parse(String.valueOf(documentJson));
                name = OrphanScan.unquote(document.get("profile_name"));
                coins = OrphanScan.number(document.get("coins"));
            } catch (Exception e) {
                Logger.warn(e, "Unreadable profile document {}", profileId);
            }
        }

        UUID coopId = CoopStorage.coopIdOfProfile(profileId);
        int members = 0;
        if (coopId != null) {
            Document roster = CoopStorage.read(coopId);
            if (roster != null) members = CoopStorage.ids(roster, CoopStorage.MEMBERS).size();
        }
        return new ProfileSummary(profileId.toString(), name, selected, coins,
                coopId == null ? null : coopId.toString(), members);
    }

    private static PunishmentSummary summarise(ActivePunishment punishment) {
        String reason = punishment.reason() == null ? "" : punishment.reason().getReasonString();
        return new PunishmentSummary(punishment.type(), punishment.banId(), reason, punishment.expiresAt());
    }

    private static @Nullable Long lastSeen(UUID uuid) {
        try {
            GetPresenceBulkProtocol.GetPresenceBulkResponse response = RedisClient.requestService(ServiceType.FRIEND,
                    new GetPresenceBulkProtocol(), new GetPresenceBulkProtocol.GetPresenceBulkMessage(List.of(uuid)))
                    .get(2, TimeUnit.SECONDS);
            if (response == null || response.presence() == null) return null;
            return response.presence().stream().filter(info -> info.getUuid().equals(uuid))
                    .map(PresenceInfo::getLastSeen).filter(seen -> seen > 0).findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static @Nullable Long wipeSnapshotAt(UUID uuid) {
        try {
            MongoCollection<Document> snapshots = AdminBackends.database().getCollection("wipe_snapshots");
            Document snapshot = snapshots.find(Filters.eq("_id", uuid.toString())).first();
            return snapshot == null ? null : snapshot.getLong("timestamp");
        } catch (Exception e) {
            Logger.warn(e, "Failed to read the wipe snapshot of {}", uuid);
            return null;
        }
    }

    private static Rank parseRank(@Nullable String stored) {
        if (stored == null) return Rank.DEFAULT;
        try {
            return Rank.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return Rank.DEFAULT;
        }
    }

    private static void release(UUID uuid) {
        try {
            if (SharedData.account().isLoaded(uuid)) SharedData.account().unload(uuid);
        } catch (Exception e) {
            Logger.warn(e, "Failed to release the account container of {}", uuid);
        }
    }
}
