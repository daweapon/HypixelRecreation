package net.swofty.type.skyblockgeneric.user.island;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.SharedInstance;
import net.swofty.commons.data.SwoftyData;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// Tracks players visiting someone else's SkyBlock island as a guest, without ever touching the
// visitor's own SkyBlockPlayerProfiles/SkyBlockDataHandler state (unlike ProfileSwitcher, which is
// for switching between a player's OWN profiles). A visit only moves the player's physical Minestom
// instance - player.getSkyBlockIsland() keeps pointing at their own island the whole time, so a plain
// reconnect/respawn with no pending visit queued naturally sends them home.
public final class VisitManager {
    private static final String PENDING_PREFIX = "hsb:visit:pending:";
    private static final String VISITORS_PREFIX = "hsb:visit:visitors:";
    private static final int PENDING_TTL_SECONDS = 30;

    // Which island (if any) a player is currently physically standing in as a guest. Keyed locally
    // since the visitor's instance is always on this JVM once they've actually spawned in.
    private static final Map<UUID, VisitSession> activeVisits = new ConcurrentHashMap<>();

    private VisitManager() {
    }

    public record VisitSession(UUID islandId, UUID hostProfileId, UUID ownerUuid) {
    }

    // Queued by /visit right before a cross-server transfer (or immediately before a same-server
    // move) so the server that ends up hosting the visitor knows to load a foreign island.
    public static void queuePendingVisit(UUID visitorUuid, UUID islandId, UUID hostProfileId, UUID ownerUuid) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            jedis.setex(PENDING_PREFIX + visitorUuid, PENDING_TTL_SECONDS, islandId + "|" + hostProfileId + "|" + ownerUuid);
        }
    }

    // Reads and clears a pending visit request - called once, right as the player spawns onto the
    // server that will actually host the visit (see ActionPlayerTeleport).
    public static VisitSession consumePendingVisit(UUID visitorUuid) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            String value = jedis.get(PENDING_PREFIX + visitorUuid);
            if (value == null) return null;
            jedis.del(PENDING_PREFIX + visitorUuid);

            String[] parts = value.split("\\|");
            if (parts.length != 3) return null;
            return new VisitSession(UUID.fromString(parts[0]), UUID.fromString(parts[1]), UUID.fromString(parts[2]));
        }
    }

    public static int visitorCount(UUID islandId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            Long count = jedis.scard(VISITORS_PREFIX + islandId);
            return count == null ? 0 : count.intValue();
        }
    }

    // Real Hypixel: "the Guests Limit before upgrades depends on the highest rank among the owners
    // of the island" - i.e. a co-op island's limit is the max across every member's own rank, not
    // just whichever member's profile happens to be looked up.
    public static int maxGuestLimit(UUID ownerProfileId, UUID ownerAccountUuid) {
        CoopDatabase.Coop coop = CoopDatabase.getFromMemberProfile(ownerProfileId);
        if (coop == null) {
            return rankOf(ownerAccountUuid).maxIslandGuests();
        }
        int max = 0;
        for (UUID member : coop.members()) {
            max = Math.max(max, rankOf(member).maxIslandGuests());
        }
        return max;
    }

    private static Rank rankOf(UUID accountUuid) {
        String raw = SwoftyData.account().get(accountUuid, HypixelDataHandler.Data.RANK.accountField());
        if (raw == null) return Rank.DEFAULT;
        try {
            return Rank.valueOf(raw.replace("\"", "").trim());
        } catch (IllegalArgumentException exception) {
            return Rank.DEFAULT;
        }
    }

    // Shared by /sbkickall and GUIGuests's Moderation button. Must be called from a virtual thread -
    // sendHome() blocks on scheduleNextTick(...).join(), same restriction as elsewhere in this class.
    public static int kickAllGuests(SkyBlockPlayer owner) {
        UUID islandId = owner.getSkyBlockIsland().getIslandID();
        List<UUID> ids = List.copyOf(visitorIds(islandId));
        int kicked = 0;
        for (UUID visitorId : ids) {
            SkyBlockPlayer target = net.swofty.type.skyblockgeneric.SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(loaded -> loaded.getUuid().equals(visitorId))
                    .findFirst().orElse(null);
            if (target == null) continue;

            sendHome(target);
            target.sendMessage("<c>You were kicked off {}'s island.", owner.getUsername());
            kicked++;
        }
        return kicked;
    }

    // Used by /sbkickall - every visitor UUID currently on this island, wherever it's hosted.
    public static Set<UUID> visitorIds(UUID islandId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            Set<String> raw = jedis.smembers(VISITORS_PREFIX + islandId);
            Set<UUID> ids = new HashSet<>();
            for (String id : raw) ids.add(UUID.fromString(id));
            return ids;
        }
    }

    public static void markVisiting(UUID visitorUuid, VisitSession session) {
        activeVisits.put(visitorUuid, session);
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            jedis.sadd(VISITORS_PREFIX + session.islandId(), visitorUuid.toString());
        }
    }

    public static VisitSession activeVisit(UUID visitorUuid) {
        return activeVisits.get(visitorUuid);
    }

    public static boolean isVisiting(UUID visitorUuid) {
        return activeVisits.containsKey(visitorUuid);
    }

    // Called on disconnect and when returning home, so the player stops counting against the
    // island's guest limit.
    public static void clearVisit(UUID visitorUuid) {
        VisitSession session = activeVisits.remove(visitorUuid);
        if (session == null) return;
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            jedis.srem(VISITORS_PREFIX + session.islandId(), visitorUuid.toString());
        }
    }

    // Tick-thread-safe instance swap, mirroring ProfileSwitcher.moveTo - setInstance()/teleport()
    // must run on the main tick thread or players can land before chunks finish sending.
    public static void moveTo(SkyBlockPlayer player, SharedInstance instance) {
        if (instance == null || player.getInstance() == instance) return;

        CompletableFuture<Void> moved = new CompletableFuture<>();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            player.setInstance(instance, player.getRespawnPoint()).join();
            player.teleport(player.getRespawnPoint());
            moved.complete(null);
        });
        moved.join();
    }

    // Shared by /is (self), /sbkick and /sbkickall (owner/coop member forcing a guest out) - clears
    // the visit and moves the player back to their own (untouched) SkyBlockIsland reference. Must be
    // called from a virtual thread, never a command's tick-thread callback directly - see moveTo().
    public static void sendHome(SkyBlockPlayer visitor) {
        clearVisit(visitor.getUuid());

        SkyBlockIsland ownIsland = visitor.getSkyBlockIsland();
        UUID host = SkyBlockIsland.hasIsland(ownIsland.getIslandID())
                ? HypixelConst.getServerUUID()
                : IslandHostLookup.findRemoteHost(ownIsland.getIslandID());

        if (host == null || host.equals(HypixelConst.getServerUUID())) {
            moveTo(visitor, ownIsland.getSharedInstance().join());
        } else {
            visitor.asProxyPlayer().transferToWithIndication(host).join();
        }
    }
}
