package dev.daweapon.type.skyblockgeneric.user.playtime;

import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;

public final class ProfilePlaytime {
    public static final long HOUR_MILLIS = Duration.ofHours(1).toMillis();
    private static final long BACKFILL_CAP_MILLIS = 10 * HOUR_MILLIS;
    private static final long INTERVAL_MILLIS = Duration.ofMinutes(1).toMillis();
    private static final int INTERVAL_TICKS = (int) (INTERVAL_MILLIS / 50);

    private ProfilePlaytime() {
    }

    public static void initialize() {
        MinecraftServer.getSchedulerManager().buildTask(() -> SkyBlockGenericLoader.forEachLoadedPlayer(
                        player -> add(player, INTERVAL_MILLIS)))
                .repeat(TaskSchedule.tick(INTERVAL_TICKS)).schedule();
    }

    public static long millis(SkyBlockPlayer player) {
        DatapointLong datapoint = datapoint(player);
        Long value = datapoint.getValue();
        if (value == null || value < 0) {
            value = backfill(player);
            datapoint.setValue(value);
        }
        return value;
    }

    public static double hours(SkyBlockPlayer player) {
        return millis(player) / (double) HOUR_MILLIS;
    }

    public static boolean atLeastHours(SkyBlockPlayer player, int hours) {
        return millis(player) >= hours * HOUR_MILLIS;
    }

    public static void ensureAtLeast(SkyBlockPlayer player, long millis) {
        if (millis(player) < millis) datapoint(player).setValue(millis);
    }

    public static String format(long millis) {
        long minutes = millis / 60_000;
        return minutes / 60 + "h " + minutes % 60 + "m";
    }

    private static void add(SkyBlockPlayer player, long millis) {
        datapoint(player).setValue(millis(player) + millis);
    }

    private static long backfill(SkyBlockPlayer player) {
        long created = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.CREATED, DatapointLong.class).getValue();
        if (created <= 0) return 0;
        return Math.clamp(System.currentTimeMillis() - created, 0, BACKFILL_CAP_MILLIS);
    }

    private static DatapointLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PROFILE_PLAYTIME, DatapointLong.class);
    }
}
