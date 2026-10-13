package net.swofty.type.skyblockgeneric.garden.pest;

public final class PestRules {
    public static final double BASE_SPAWN_CHANCE_PERCENT = 0.2;
    public static final int BASE_COOLDOWN_SECONDS = 300;
    public static final long SPRAY_DURATION_MILLIS = 30 * 60_000L;
    public static final long REPELLENT_DURATION_MILLIS = 60 * 60_000L;
    public static final int SPRAY_BONUS_CHANCE = 50;
    public static final double SPRAYED_SPAWN_MULTIPLIER = 2;
    public static final int SPRAY_ATTRACT_WEIGHT = 1000;
    public static final int BASE_WEIGHT = 100;
    public static final long OFFLINE_SPAWN_MILLIS = 60 * 60_000L;
    public static final int FORTUNE_PER_TRADED_PEST = 5;
    public static final int MAX_TRADED_PESTS = 40;
    public static final long TRADE_BOOST_MILLIS = 30 * 60_000L;
    public static final int MIN_GARDEN_LEVEL = 5;
    public static final int COOLDOWN_REDUCTION_PER_PESTHUNTER_PIECE = 10;

    private static final int[][] LOSS_PERCENT = {
            {5, 0, 0, 0, 0, 0},
            {15, 5, 0, 0, 0, 0},
            {30, 15, 5, 0, 0, 0},
            {50, 30, 15, 5, 0, 0},
            {75, 50, 30, 15, 5, 0}
    };

    private PestRules() {
    }

    public static int fortuneLossPercent(int pests, int bonusChance) {
        if (pests < 4) return 0;
        int row = Math.min(pests, PestData.MAX_ALIVE) - 4;
        int band = Math.min(LOSS_PERCENT[row].length - 1, Math.max(0, bonusChance) / 100);
        return LOSS_PERCENT[row][band];
    }

    public static int pestsPerSpawn(int bonusChance, double roll) {
        int bonus = Math.max(0, bonusChance);
        return 1 + bonus / 100 + (roll * 100 < bonus % 100 ? 1 : 0);
    }

    public static double spawnChancePercent(boolean sprayed) {
        return BASE_SPAWN_CHANCE_PERCENT * (sprayed ? SPRAYED_SPAWN_MULTIPLIER : 1);
    }

    public static long cooldownMillis(boolean repellent, boolean repellentMax, int reductionPercent) {
        int factor = repellentMax ? 4 : repellent ? 2 : 1;
        double reduction = 1 - Math.min(90, Math.max(0, reductionPercent)) / 100.0;
        return Math.round(BASE_COOLDOWN_SECONDS * 1000L * reduction) * factor;
    }

    public static int tradeFortune(int pests) {
        return Math.min(MAX_TRADED_PESTS, Math.max(0, pests)) * FORTUNE_PER_TRADED_PEST;
    }
}
