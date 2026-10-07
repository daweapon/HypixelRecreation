package net.swofty.type.skyblockgeneric.museum;

public final class MuseumMilestones {
    public static final int MAX_MILESTONE = 40;
    public static final int XP_PER_MILESTONE = 100;
    public static final double BITS_MULTIPLIER_PER_MILESTONE = 0.01D;
    public static final double BANK_INTEREST_PER_MILESTONE = 0.02D;
    public static final int APPRAISAL_MILESTONE = 3;
    public static final double APPRAISAL_COST = 10_000_000D;

    private MuseumMilestones() {}

    public static int milestoneFor(int museumXp) {
        return Math.clamp(museumXp / XP_PER_MILESTONE, 0, MAX_MILESTONE);
    }

    public static int requiredXp(int milestone) {
        return milestone * XP_PER_MILESTONE;
    }

    public static int clamp(int milestone) {
        return Math.clamp(milestone, 0, MAX_MILESTONE);
    }

    public static double bankInterestMultiplier(int milestone) {
        return 1D + clamp(milestone) * BANK_INTEREST_PER_MILESTONE;
    }

    public static double bitsMultiplierBonus(int milestone) {
        return clamp(milestone) * BITS_MULTIPLIER_PER_MILESTONE;
    }
}
