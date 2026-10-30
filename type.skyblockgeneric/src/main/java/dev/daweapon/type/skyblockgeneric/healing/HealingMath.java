package dev.daweapon.type.skyblockgeneric.healing;

public final class HealingMath {
    public static final double BASE_VITALITY = 100;
    public static final double REGEN_FRACTION_PER_SECOND = 0.05;
    public static final double SET_BONUS_PERCENT = 50;
    public static final long SET_WORN_MILLIS = 3_000;

    private HealingMath() {
    }

    public static double maxVitality(double vitalityStat) {
        return Math.max(BASE_VITALITY, vitalityStat);
    }

    public static double regenerated(double current, double max) {
        return Math.max(0, Math.min(max, current + max * REGEN_FRACTION_PER_SECOND));
    }

    public static double multiplier(boolean setBonusActive, double habaneroPercent) {
        return 1 + ((setBonusActive ? SET_BONUS_PERCENT : 0) + Math.max(0, habaneroPercent)) / 100D;
    }

    public static double tickHeal(double perSecond, double multiplier) {
        return Math.max(0, perSecond) * Math.max(0, multiplier);
    }

    public static boolean setBonusActive(boolean wearingFullSet, long wornSinceMillis, long nowMillis) {
        return wearingFullSet && wornSinceMillis > 0 && nowMillis - wornSinceMillis >= SET_WORN_MILLIS;
    }
}
