package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

public final class BeaconRules {
    public static final int MAX_LEVEL = 10;
    public static final int BASE_STACK_LIMIT = 5;
    public static final int MAX_STACK_LIMIT = 10;
    public static final double BASE_FORTUNE_PER_STACK = 5;
    public static final double FORTUNE_PER_LEVEL = 0.5;
    public static final long STACK_MILLIS = 20L * 60 * 1000;
    public static final double BEACONMITE_BASE_PERCENT = 10;
    public static final double BEACONMITE_PERCENT_PER_LEVEL = 1;
    public static final int NORMAL_PANELS = 1;
    public static final int ENHANCED_PANELS = 2;

    private BeaconRules() {
    }

    public static int stackLimit(int level) {
        return Math.min(MAX_STACK_LIMIT, BASE_STACK_LIMIT + Math.max(0, level) / 2);
    }

    public static double fortunePerStack(int level) {
        return BASE_FORTUNE_PER_STACK + FORTUNE_PER_LEVEL * Math.max(0, Math.min(MAX_LEVEL, level));
    }

    public static double fortune(int activeStacks, int level, double zealotPercent) {
        int effective = Math.min(Math.max(0, activeStacks), stackLimit(level));
        return effective * fortunePerStack(level) * (1 + Math.max(0, zealotPercent) / 100D);
    }

    public static double beaconmitePercent(int level) {
        return BEACONMITE_BASE_PERCENT + BEACONMITE_PERCENT_PER_LEVEL * Math.max(0, Math.min(MAX_LEVEL, level));
    }

    public static String format(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    public static int panels(boolean enhanced) {
        return enhanced ? ENHANCED_PANELS : NORMAL_PANELS;
    }
}
