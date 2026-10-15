package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;

import java.time.Duration;

/**
 * Hypixel's Bits Available pool (wiki: Bits). Each Booster Cookie adds 4,800 × Bits Multiplier to
 * the pool, and while the Cookie Buff is active the pool pays 250 × Bits Multiplier into the
 * purse every 30 minutes. A player who comes back after 30 minutes or more gets one set at once.
 */
public final class BitsService {
    private static final int BITS_PER_COOKIE = 4_800;
    private static final int BITS_PER_SET = 250;
    private static final long SET_INTERVAL_MS = Duration.ofMinutes(30).toMillis();

    private BitsService() {
    }

    public static void startFarming() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.forEachLoadedPlayer(BitsService::farm))
                .repeat(TaskSchedule.minutes(1))
                .schedule();
    }

    public static int perCookie(SkyBlockPlayer player) {
        return (int) Math.round(BITS_PER_COOKIE * BitsMultiplier.of(player));
    }

    public static void addCookie(SkyBlockPlayer player) {
        int available = available(player) + perCookie(player);
        setInt(player, SkyBlockDataHandler.Data.BITS_AVAILABLE, available);
        setInt(player, SkyBlockDataHandler.Data.BITS_POOL_SIZE, available);
        if (lastFarmed(player) == 0) setLastFarmed(player, System.currentTimeMillis());
    }

    public static int available(SkyBlockPlayer player) {
        return getInt(player, SkyBlockDataHandler.Data.BITS_AVAILABLE);
    }

    public static int poolSize(SkyBlockPlayer player) {
        return Math.max(available(player), getInt(player, SkyBlockDataHandler.Data.BITS_POOL_SIZE));
    }

    static void farm(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null || !player.isBoosterCookieActive()) return;
        int available = available(player);
        if (available <= 0) return;
        long now = System.currentTimeMillis();
        if (now - lastFarmed(player) < SET_INTERVAL_MS) return;

        int gained = Math.min(available, (int) Math.round(BITS_PER_SET * BitsMultiplier.of(player)));
        setInt(player, SkyBlockDataHandler.Data.BITS_AVAILABLE, available - gained);
        setLastFarmed(player, now);
        player.addBits(AccessoryEconomyHooks.bitsGained(player, gained));
    }

    private static int getInt(SkyBlockPlayer player, SkyBlockDataHandler.Data data) {
        return player.getSkyblockDataHandler().get(data, DatapointInteger.class).getValue();
    }

    private static void setInt(SkyBlockPlayer player, SkyBlockDataHandler.Data data, int value) {
        player.getSkyblockDataHandler().get(data, DatapointInteger.class).setValue(value);
    }

    private static long lastFarmed(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS_LAST_FARMED, DatapointLong.class).getValue();
    }

    private static void setLastFarmed(SkyBlockPlayer player, long value) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BITS_LAST_FARMED, DatapointLong.class).setValue(value);
    }
}
