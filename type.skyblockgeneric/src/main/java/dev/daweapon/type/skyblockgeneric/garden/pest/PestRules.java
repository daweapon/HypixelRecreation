package dev.daweapon.type.skyblockgeneric.garden.pest;

public final class PestRules {
    public static final double BASE_SPAWN_CHANCE_PERCENT = 0.2;
    public static final int BASE_COOLDOWN_SECONDS = 300;
    public static final long MIN_COOLDOWN_MILLIS = 130_000L;
    public static final long MIN_COOLDOWN_ERADICATOR_MILLIS = 70_000L;
    public static final long MAX_COOLDOWN_MILLIS = 20 * 60_000L;
    public static final long REPELLENT_DURATION_MILLIS = 60 * 60_000L;
    public static final int SPRAY_ATTRACT_WEIGHT = 1000;
    public static final int VINYL_ATTRACT_WEIGHT = 1000;
    public static final int BASE_WEIGHT = 100;
    public static final long OFFLINE_SPAWN_MILLIS = 60 * 60_000L;
    public static final int FORTUNE_PER_TRADED_PEST = 5;
    public static final int MAX_TRADED_PESTS = 40;
    public static final long TRADE_BOOST_MILLIS = 30 * 60_000L;
    public static final int MIN_GARDEN_LEVEL = 5;
    public static final int COOLDOWN_REDUCTION_PER_PESTHUNTER_PIECE = 10;
    public static final int COOLDOWN_REDUCTION_PEST_VEST = 15;
    public static final double COOLDOWN_REDUCTION_SQUEAKY = 2.5;
    public static final int BOOKWORM_DAMAGE = 20;
    public static final int MAX_VACUUM_BOOKS = 5;
    public static final int FARMING_FOR_DUMMIES_FORTUNE = 1;
    public static final double BUZZING_DAMAGE_MULTIPLIER = 2;
    public static final double BEADY_PEST_FORTUNE = 100;
    private static final int[] ERADICATOR_FORTUNE = {0, 0, 50, 75, 100};

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

    public static double spawnChancePercent(double sprayMultiplier) {
        return BASE_SPAWN_CHANCE_PERCENT * Math.max(1, sprayMultiplier);
    }

    public static int eradicatorFortune(int pieces) {
        return ERADICATOR_FORTUNE[Math.min(Math.max(0, pieces), ERADICATOR_FORTUNE.length - 1)];
    }

    public static double vinylWeight(double mosquitoBoostFraction) {
        return VINYL_ATTRACT_WEIGHT * (1 + Math.max(0, mosquitoBoostFraction));
    }

    public static double vacuumDamage(double tierDamage, int bookwormBooks, double reforgeDamage, boolean buzzing) {
        double damage = tierDamage + Math.min(MAX_VACUUM_BOOKS, Math.max(0, bookwormBooks)) * BOOKWORM_DAMAGE + reforgeDamage;
        return buzzing ? damage * BUZZING_DAMAGE_MULTIPLIER : damage;
    }

    public static long cooldownMillis(boolean repellent, boolean repellentMax, double reductionPercent) {
        return cooldownMillis(repellent, repellentMax, reductionPercent, false);
    }

    public static long cooldownMillis(boolean repellent, boolean repellentMax, double reductionPercent, boolean eradicator) {
        int factor = repellentMax ? 4 : repellent ? 2 : 1;
        double reduction = 1 - Math.min(90, Math.max(0, reductionPercent)) / 100.0;
        long reduced = Math.max(Math.round(BASE_COOLDOWN_SECONDS * 1000L * reduction),
                eradicator ? MIN_COOLDOWN_ERADICATOR_MILLIS : MIN_COOLDOWN_MILLIS);
        return Math.min(MAX_COOLDOWN_MILLIS, reduced * factor);
    }

    public static long tradeBoostMillis(boolean pestEradicator) {
        return pestEradicator ? 2 * TRADE_BOOST_MILLIS : TRADE_BOOST_MILLIS;
    }

    public static int tradeFortune(int pests) {
        return Math.min(MAX_TRADED_PESTS, Math.max(0, pests)) * FORTUNE_PER_TRADED_PEST;
    }
}
