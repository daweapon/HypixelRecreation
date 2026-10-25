package dev.daweapon.type.skyblockgeneric.greatspook;

public final class PrimalFearRules {
    public static final long BASE_COOLDOWN_MILLIS = 6 * 60_000L;
    public static final long COOLDOWN_REDUCTION_PER_FEAR_MILLIS = 3_000L;
    public static final long MINIMUM_COOLDOWN_MILLIS = 15_000L;
    public static final double BASE_CHANCE_PERCENT = 10;
    public static final double CHANCE_PER_FEAR_PERCENT = 0.1;
    public static final double MAXIMUM_CHANCE_PERCENT = 50;
    public static final double FEARAMA_PER_FEAR = 0.01;
    public static final double MAXIMUM_DAMAGE_REDUCTION = 1;
    public static final int HEALTH = 5;

    private PrimalFearRules() {
    }

    public static long cooldownMillis(double fear) {
        long reduced = BASE_COOLDOWN_MILLIS - (long) (Math.max(0, fear) * COOLDOWN_REDUCTION_PER_FEAR_MILLIS);
        return Math.max(MINIMUM_COOLDOWN_MILLIS, reduced);
    }

    public static long remainingMillis(long lastSpawn, double fear, long now) {
        return Math.max(0, lastSpawn + cooldownMillis(fear) - now);
    }

    public static double chancePercent(double fear) {
        return Math.min(MAXIMUM_CHANCE_PERCENT, BASE_CHANCE_PERCENT + Math.max(0, fear) * CHANCE_PER_FEAR_PERCENT);
    }

    public static float hitDamage(double fear, boolean fearama) {
        return (float) (1 + (fearama ? Math.max(0, fear) * FEARAMA_PER_FEAR : 0));
    }

    public static double damageTakenMultiplier(double fear) {
        return 1 - Math.min(MAXIMUM_DAMAGE_REDUCTION, Math.max(0, fear) / 100D);
    }

    public static int skulls(double health) {
        return (int) Math.max(0, Math.ceil(health));
    }

    public static double creasingFear(double perTenKills, long kills) {
        return perTenKills * (Math.min(kills, 150) / 10);
    }
}
