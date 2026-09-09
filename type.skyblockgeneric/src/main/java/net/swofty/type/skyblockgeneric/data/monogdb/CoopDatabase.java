package net.swofty.type.skyblockgeneric.data.monogdb;

import com.mongodb.client.MongoClient;
import net.swofty.commons.skyblock.CoopStorage;
import net.swofty.proxyapi.ProxyPlayerSet;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

// Reads and writes coop rosters (members, invites, linked profiles) to/from shared coop storage.
public class CoopDatabase {

    public static void connect(MongoClient client) {
    }

    public void save(Coop coop) {
        CoopStorage.write(coop.serialize());
    }

    public static Coop update(UUID coopId, Consumer<Coop> mutation) {
        Document mutated = CoopStorage.update(coopId, stored -> {
            Coop coop = Coop.deserialize(stored);
            mutation.accept(coop);
            return coop.serialize();
        });
        return mutated == null ? null : Coop.deserialize(mutated);
    }

    // bankBalance is a plain long on the (immutable) Coop record, so unlike the mutable member
    // lists it can't be adjusted in place through update()'s Consumer<Coop> - this mutates the
    // stored Document field directly instead, under the same roster lock CoopStorage.update() uses.
    public static Coop adjustBankBalance(UUID coopId, long delta) {
        Document mutated = CoopStorage.update(coopId, stored -> {
            stored.put("bankBalance", Coop.readBankBalance(stored) + delta);
            return stored;
        });
        return mutated == null ? null : Coop.deserialize(mutated);
    }

    // Withdraws only if the balance is (still) sufficient at the moment the roster lock is held,
    // so two concurrent withdrawals can never together push the balance negative. Returns null if
    // the coop doesn't exist or the balance was insufficient.
    public static Coop tryWithdraw(UUID coopId, long amount) {
        AtomicBoolean sufficientFunds = new AtomicBoolean(true);
        Document mutated = CoopStorage.update(coopId, stored -> {
            long current = Coop.readBankBalance(stored);
            if (current < amount) {
                sufficientFunds.set(false);
                return stored;
            }
            stored.put("bankBalance", current - amount);
            return stored;
        });
        if (!sufficientFunds.get() || mutated == null) return null;
        return Coop.deserialize(mutated);
    }

    public static Coop getFromMember(UUID member) {
        return deserialize(CoopStorage.readByMember(member));
    }

    public static Coop get(UUID coopId) {
        return deserialize(CoopStorage.read(coopId));
    }

    public static Coop getFromMemberProfile(UUID memberProfile) {
        return deserialize(CoopStorage.readByProfile(memberProfile));
    }

    public static boolean isMember(Coop coop, UUID player) {
        return coop != null && coop.members().contains(player);
    }

    private static Coop deserialize(Document roster) {
        return roster == null ? null : Coop.deserialize(roster);
    }

    public static Coop getClean(UUID originator) {
        return new Coop(UUID.randomUUID(), originator, new ArrayList<>(List.of(originator)), new ArrayList<>(), new ArrayList<>(),
                0L, new HashMap<>(), new HashMap<>());
    }

    // Stamped on disconnect for co-op members so a 2-person co-op can use the real-Hypixel
    // 35-days-inactive bypass when a normal kick vote isn't possible (see CoopVoteManager).
    public static void markLastSeen(UUID coopId, UUID member) {
        update(coopId, latest -> latest.lastSeen().put(member, System.currentTimeMillis()));
    }

    public static long getLastSeen(Coop coop, UUID member) {
        return coop.lastSeen().getOrDefault(member, 0L);
    }

    public record Coop(UUID coopUUID, UUID originator, List<UUID> members, List<UUID> memberInvites,
                       List<UUID> memberProfiles, long bankBalance, Map<UUID, Long> lastSeen, Map<UUID, Long> contributions) {
        public ProxyPlayerSet getMembersAsProxyPlayerSet(UUID... toExclude) {
            List<UUID> members = new ArrayList<>(this.members);
            members.removeAll(List.of(toExclude));
            return new ProxyPlayerSet(members);
        }

        public boolean isOriginator(UUID uuid) {
            return uuid.equals(originator);
        }

        public long getContribution(UUID uuid) {
            return contributions.getOrDefault(uuid, 0L);
        }

        public void addInvite(UUID uuid) {
            memberInvites.add(uuid);
        }

        public void removeInvite(UUID uuid) {
            memberInvites.remove(uuid);
        }

        public boolean acceptInvite(UUID uuid) {
            if (!memberInvites.contains(uuid)) return false;
            removeInvite(uuid);
            if (!members.contains(uuid)) members.add(uuid);
            return true;
        }

        public Document serialize() {
            Document document = new Document("_id", coopUUID.toString());
            document.put("originator", originator.toString());

            List<String> members = new ArrayList<>();
            this.members.forEach(uuid -> members.add(uuid.toString()));
            document.put("members", members);
            List<String> memberInvites = new ArrayList<>();
            this.memberInvites.forEach(uuid -> memberInvites.add(uuid.toString()));
            document.put("memberInvites", memberInvites);
            List<String> memberProfiles = new ArrayList<>();
            this.memberProfiles.forEach(uuid -> memberProfiles.add(uuid.toString()));
            document.put("memberProfiles", memberProfiles);
            document.put("bankBalance", bankBalance);

            Document lastSeenDoc = new Document();
            this.lastSeen.forEach((uuid, timestamp) -> lastSeenDoc.put(uuid.toString(), timestamp));
            document.put("lastSeen", lastSeenDoc);

            Document contributionsDoc = new Document();
            this.contributions.forEach((uuid, amount) -> contributionsDoc.put(uuid.toString(), amount));
            document.put("contributions", contributionsDoc);

            return document;
        }

        public List<SkyBlockPlayer> getOnlineInvitedPlayers() {
            return SkyBlockGenericLoader.getLoadedPlayers().stream().filter(player -> memberInvites.contains(player.getUuid())).toList();
        }

        public List<SkyBlockPlayer> getOnlineMembers() {
            return SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(player -> members.contains(player.getUuid()))
                    .filter(player -> memberProfiles.contains(player.getProfiles().getCurrentlySelected()))
                    .toList();
        }

        public void save() {
            CoopDatabase database = new CoopDatabase();
            database.save(this);
        }

        public Boolean isSameAs(Coop coop) {
            return coop.coopUUID.equals(coopUUID);
        }

        public static Coop deserialize(Document document) {
            UUID coopUUID = UUID.fromString(document.getString("_id"));
            UUID originator = UUID.fromString(document.getString("originator"));

            List<UUID> members = new ArrayList<>();
            List<String> membersString = (List<String>) document.get("members");
            membersString.forEach(uuid -> members.add(UUID.fromString(uuid)));
            List<UUID> memberInvites = new ArrayList<>();
            List<String> memberInvitesString = (List<String>) document.get("memberInvites");
            memberInvitesString.forEach(uuid -> memberInvites.add(UUID.fromString(uuid)));
            List<UUID> memberProfiles = new ArrayList<>();
            List<String> memberProfilesString = (List<String>) document.get("memberProfiles");
            memberProfilesString.forEach(uuid -> memberProfiles.add(UUID.fromString(uuid)));

            return new Coop(coopUUID, originator, members, memberInvites, memberProfiles, readBankBalance(document),
                    readLastSeen(document), readContributions(document));
        }

        // Existing coop documents predate the bank feature and won't have this field yet.
        private static long readBankBalance(Document document) {
            Object raw = document.get("bankBalance");
            return raw == null ? 0L : ((Number) raw).longValue();
        }

        // Existing coop documents predate lastSeen/contributions and won't have these fields yet.
        private static Map<UUID, Long> readLastSeen(Document document) {
            Map<UUID, Long> lastSeen = new HashMap<>();
            Object raw = document.get("lastSeen");
            if (raw instanceof Document lastSeenDoc) {
                lastSeenDoc.forEach((key, value) -> lastSeen.put(UUID.fromString(key), ((Number) value).longValue()));
            }
            return lastSeen;
        }

        private static Map<UUID, Long> readContributions(Document document) {
            Map<UUID, Long> contributions = new HashMap<>();
            Object raw = document.get("contributions");
            if (raw instanceof Document contributionsDoc) {
                contributionsDoc.forEach((key, value) -> contributions.put(UUID.fromString(key), ((Number) value).longValue()));
            }
            return contributions;
        }
    }
}
