package dev.daweapon.type.skyblockgeneric.theend;

public final class SpecialZealotOdds {
    public static final int PITY_KILLS = 400;
    public static final double ZEALOT_ODDS = 1D / 420;
    public static final double BRUISER_MULTIPLIER = 1.105;
    public static final double ZEALUCK_PERCENT_PER_TIER = 2;

    private SpecialZealotOdds() {
    }

    public static double chancePercent(boolean bruiserKill, double petBonusPercent, double zealuckPercent) {
        double odds = ZEALOT_ODDS;
        if (bruiserKill) odds *= BRUISER_MULTIPLIER;
        odds *= 1 + petBonusPercent / 100;
        odds *= 1 + zealuckPercent / 100;
        return odds * 100;
    }

    public static double zealuckPercent(int tier) {
        return Math.clamp(tier, 0, 5) * ZEALUCK_PERCENT_PER_TIER;
    }

    public static boolean pityReached(int killsSinceSpecialZealot) {
        return killsSinceSpecialZealot >= PITY_KILLS;
    }
}
