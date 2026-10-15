package dev.daweapon.dungeons.catacombs.level;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CatacombsLevels {
    public static final int MAXIMUM_LEVEL = 50;
    public static final int HEALTH_PER_LEVEL = 2;
    public static final int DAILY_BONUS_RUNS = 5;
    public static final int DAILY_BONUS_PERCENT = 40;

    private static final long[] XP_PER_LEVEL = {
            50, 75, 110, 160, 230, 330, 470, 670, 950, 1_340,
            1_890, 2_665, 3_760, 5_260, 7_380, 10_300, 14_400, 20_000, 27_600, 38_000,
            52_500, 71_500, 97_000, 132_000, 180_000, 243_000, 328_000, 445_000, 600_000, 800_000,
            1_065_000, 1_410_000, 1_900_000, 2_500_000, 3_300_000, 4_300_000, 5_600_000, 7_200_000, 9_200_000, 12_000_000,
            15_000_000, 19_000_000, 24_000_000, 30_000_000, 38_000_000, 48_000_000, 60_000_000, 75_000_000, 93_000_000, 116_250_000
    };

    private static final long[] CUMULATIVE_XP = new long[MAXIMUM_LEVEL + 1];

    static {
        for (int level = 1; level <= MAXIMUM_LEVEL; level++) {
            CUMULATIVE_XP[level] = CUMULATIVE_XP[level - 1] + XP_PER_LEVEL[level - 1];
        }
    }

    public static long xpForLevel(int level) {
        if (level < 0 || level > MAXIMUM_LEVEL) {
            throw new IllegalArgumentException("Catacombs level must be between 0 and " + MAXIMUM_LEVEL);
        }
        return CUMULATIVE_XP[level];
    }

    public static long xpToReachLevel(int level) {
        if (level < 1 || level > MAXIMUM_LEVEL) {
            throw new IllegalArgumentException("Catacombs level must be between 1 and " + MAXIMUM_LEVEL);
        }
        return XP_PER_LEVEL[level - 1];
    }

    public static int levelForXp(long xp) {
        int level = 0;
        while (level < MAXIMUM_LEVEL && xp >= CUMULATIVE_XP[level + 1]) {
            level++;
        }
        return level;
    }

    public static int healthBonus(int level) {
        return Math.min(MAXIMUM_LEVEL, Math.max(0, level)) * HEALTH_PER_LEVEL;
    }
}
