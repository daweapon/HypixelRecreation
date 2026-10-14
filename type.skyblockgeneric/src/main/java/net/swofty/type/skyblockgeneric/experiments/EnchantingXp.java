package net.swofty.type.skyblockgeneric.experiments;

public final class EnchantingXp {
    public static final long DAILY_MANUAL_CAP = 500_000;
    private static final double LEVEL_COEFFICIENT = 3.5;

    private EnchantingXp() {
    }

    public static double forLevelsSpent(int levels) {
        return levels <= 0 ? 0 : LEVEL_COEFFICIENT * Math.pow(levels, 1.5);
    }

    public static double withinDailyCap(double xp, long alreadyToday) {
        return Math.max(0, Math.min(xp, DAILY_MANUAL_CAP - alreadyToday));
    }

    public static long orbsFromBottle(long baseOrbs, int enchantingLevel) {
        return Math.round(baseOrbs * (1 + 0.05 * enchantingLevel));
    }
}
