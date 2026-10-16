package dev.daweapon.type.skyblockgeneric.social;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SocialXp {
    public static final double UNIQUE_GUEST_XP = 10;
    public static final double HOST_MINUTE_XP_PER_GUEST = 2;
    public static final double GUEST_MINUTE_XP = 1;
    private static final String UNIQUE_GUEST_PREFIX = "hsb:social:guest:";
    private static final int UNIQUE_GUEST_TTL_SECONDS = 24 * 60 * 60;

    private static boolean started;

    private SocialXp() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(SocialXp::tickMinute)
                .delay(TaskSchedule.minutes(1))
                .repeat(TaskSchedule.minutes(1))
                .schedule();
    }

    public static void onGuestArrived(UUID visitorUuid, VisitManager.VisitSession session) {
        for (SkyBlockPlayer host : hostsOf(session.islandId())) {
            if (host.getUuid().equals(visitorUuid) || !firstVisitToday(host.getUuid(), visitorUuid)) continue;
            award(host, UNIQUE_GUEST_XP);
        }
    }

    private static void tickMinute() {
        List<SkyBlockPlayer> players = new ArrayList<>(SkyBlockGenericLoader.getLoadedPlayers());
        for (SkyBlockPlayer host : players) {
            if (VisitManager.isVisiting(host.getUuid()) || host.getInstance() == null) continue;
            UUID islandId = host.getSkyBlockIsland().getIslandID();
            long guests = players.stream().filter(guest -> isGuestOn(guest, islandId, host)).count();
            if (guests == 0) continue;
            award(host, HOST_MINUTE_XP_PER_GUEST * guests);
            for (SkyBlockPlayer guest : players) {
                if (isGuestOn(guest, islandId, host)) award(guest, GUEST_MINUTE_XP);
            }
        }
    }

    private static boolean isGuestOn(SkyBlockPlayer guest, UUID islandId, SkyBlockPlayer host) {
        VisitManager.VisitSession session = VisitManager.activeVisit(guest.getUuid());
        return session != null && session.islandId().equals(islandId) && guest.getInstance() == host.getInstance();
    }

    private static List<SkyBlockPlayer> hostsOf(UUID islandId) {
        List<SkyBlockPlayer> hosts = new ArrayList<>();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (!VisitManager.isVisiting(player.getUuid()) && player.getSkyBlockIsland().getIslandID().equals(islandId)) {
                hosts.add(player);
            }
        }
        return hosts;
    }

    private static boolean firstVisitToday(UUID hostUuid, UUID visitorUuid) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            return jedis.set(UNIQUE_GUEST_PREFIX + hostUuid + ":" + visitorUuid, "1",
                    SetParams.setParams().nx().ex(UNIQUE_GUEST_TTL_SECONDS)) != null;
        }
    }

    private static void award(SkyBlockPlayer player, double xp) {
        if (player.getSkyblockDataHandler() == null) return;
        player.getSkills().increase(player, SkillCategories.SOCIAL, xp);
    }
}
