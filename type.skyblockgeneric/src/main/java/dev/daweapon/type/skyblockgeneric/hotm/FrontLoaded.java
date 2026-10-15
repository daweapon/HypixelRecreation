package dev.daweapon.type.skyblockgeneric.hotm;

import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrystalHollows;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class FrontLoaded {
    public static final int DAILY_GEMSTONES = 2_500;
    public static final double POWDER_MULTIPLIER = 3;
    public static final double GEMSTONE_FORTUNE = 150;
    public static final double MINING_SPEED = 250;
    private static final String DAY_KEY = "front_loaded_day";
    private static final String USED_KEY = "front_loaded_used";

    private FrontLoaded() {
    }

    public static long remaining(SkyBlockPlayer player) {
        if (!HotmService.hasPerk(player, "front_loaded")) return 0;
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        if (data.counter(DAY_KEY) != HotmService.dailyBonusDay()) return DAILY_GEMSTONES;
        return Math.max(0, DAILY_GEMSTONES - data.counter(USED_KEY));
    }

    public static boolean active(SkyBlockPlayer player) {
        return remaining(player) > 0;
    }

    public static void consume(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        long today = HotmService.dailyBonusDay();
        if (data.counter(DAY_KEY) != today) {
            data.getCounters().put(DAY_KEY, today);
            data.getCounters().put(USED_KEY, 0L);
        }
        data.addCounter(USED_KEY, 1);
        CrystalHollowsAccess.save(player, data);
    }
}
