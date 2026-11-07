package dev.daweapon.type.skyblockgeneric.hotm;

import dev.daweapon.type.skyblockgeneric.levels.DiminishingXp;

/**
 * Heart of the Mountain SkyBlock XP for powder earned (wiki: SkyBlock Levels/Tasks): +1 XP per 2,400 Mithril or
 * 2,500 Gemstone/Glacite Powder up to 600,000, then a shrinking amount up to 12,500,000 Mithril or 20,000,000
 * Gemstone/Glacite. The wiki gives 1,942 XP for the three together but not the split: Mithril is taken as 620,
 * the same as Forest Whispers, whose rule is identical, leaving 661 each for Gemstone and Glacite.
 */
public final class PowderExperience {
    private static final DiminishingXp MITHRIL = new DiminishingXp(2_400, 600_000, 12_500_000, 620);
    private static final DiminishingXp GEMSTONE = new DiminishingXp(2_500, 600_000, 20_000_000, 661);
    private static final DiminishingXp GLACITE = new DiminishingXp(2_500, 600_000, 20_000_000, 661);

    private PowderExperience() {
    }

    /** Whole XP only, so mining a block never moves the SkyBlock Level by a fraction. */
    public static double experience(HotmPowder powder, long earned) {
        return Math.floor(rule(powder).experience(earned));
    }

    public static double maxXp(HotmPowder powder) {
        return rule(powder).maxXp();
    }

    private static DiminishingXp rule(HotmPowder powder) {
        return switch (powder) {
            case MITHRIL -> MITHRIL;
            case GEMSTONE -> GEMSTONE;
            case GLACITE -> GLACITE;
        };
    }
}
