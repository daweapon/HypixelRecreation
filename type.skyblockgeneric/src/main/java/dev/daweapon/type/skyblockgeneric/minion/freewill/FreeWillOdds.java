package dev.daweapon.type.skyblockgeneric.minion.freewill;

public final class FreeWillOdds {
    public static final double BASE_STAY_PERCENT = 50;
    public static final double STAY_PERCENT_PER_TIER = 4;
    public static final double LOYALTY_SPEED_PERCENT = 10;
    public static final double POSTCARD_SPEED_PERCENT = 5;

    private FreeWillOdds() {
    }

    public static double stayChancePercent(int tier) {
        return Math.min(100, BASE_STAY_PERCENT + STAY_PERCENT_PER_TIER * (Math.max(1, tier) - 1));
    }
}
