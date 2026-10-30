package dev.daweapon.type.skyblockgeneric.glacite.corpse;

public final class CorpseCount {
    public static final int HARD_MAX = 4;
    private static final int SMALL_SHAFT_MODULO = 16;

    private CorpseCount() {
    }

    public static int base(String layoutCode, int spots) {
        if (spots <= 0) return 0;
        if (layoutCode.startsWith("FAIR")) return 1;
        if (layoutCode.startsWith("LITT")) return Math.min(3, spots);
        int size = switch (Math.floorMod(layoutCode.hashCode(), SMALL_SHAFT_MODULO)) {
            case 0 -> 1;
            case 1, 2, 3, 4, 5 -> 2;
            default -> 3;
        };
        return Math.min(size, spots);
    }

    public static int total(int base, int spots, boolean bonus) {
        return Math.min(HARD_MAX, Math.min(spots, base + (bonus ? 1 : 0)));
    }

    public static double bonusChancePercent(double deadMansChestPercent, int milestonesClaimed) {
        return deadMansChestPercent + CorpseMilestones.bonusCorpsePercent(milestonesClaimed);
    }

    public static double bonusRollPercent(double giftsPercent, int milestonesClaimed) {
        return giftsPercent + CorpseMilestones.bonusRollPercent(milestonesClaimed);
    }
}
