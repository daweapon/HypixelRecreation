package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HollowsSpawnProtection {
    public static final long DURATION_MILLIS = 5L * 60 * 1000;

    private static final Map<UUID, Long> EXPIRES = new ConcurrentHashMap<>();
    private static boolean started;

    private HollowsSpawnProtection() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(HollowsSpawnProtection::tick).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public static void grant(SkyBlockPlayer player) {
        EXPIRES.put(player.getUuid(), System.currentTimeMillis() + DURATION_MILLIS);
    }

    public static long millisLeft(UUID player) {
        return Math.max(0, EXPIRES.getOrDefault(player, 0L) - System.currentTimeMillis());
    }

    public static boolean isProtected(SkyBlockPlayer player) {
        return millisLeft(player.getUuid()) > 0;
    }

    public static String format(long millis) {
        long seconds = (millis + 999) / 1000;
        return "%d:%02d".formatted(seconds / 60, seconds % 60);
    }

    private static void tick() {
        EXPIRES.entrySet().removeIf(entry -> entry.getValue() <= System.currentTimeMillis());
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            long left = millisLeft(player.getUuid());
            if (left <= 0) continue;
            SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.PROTECTION,
                    Text.of("<a>Spawn Protection {}", format(left)), 30, 1);
        }
    }
}
