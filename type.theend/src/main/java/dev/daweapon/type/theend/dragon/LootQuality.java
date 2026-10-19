package dev.daweapon.type.theend.dragon;

public final class LootQuality {
    public static final int EYE_BONUS = 100;
    public static final int FRAGMENT_COST = 22;
    public static final int MAX_FRAGMENTS = 32;

    private static final int[] TOP_BASE = {300, 250, 200, 125, 110, 100, 100, 100, 90, 90, 80, 80};
    private static final int[] TOP_PEARLS = {30, 25, 22, 20, 18, 15, 15, 15, 15, 15, 15, 15};
    private static final int[] TOP_ENCHANTED = {7, 6, 5, 4, 3, 2, 2, 2, 2, 2, 2, 2};
    private static final int LAST_RANKED_PLACE = 25;

    private LootQuality() {
    }

    public static int placementBase(int rank, double damage) {
        if (rank <= TOP_BASE.length) return TOP_BASE[rank - 1];
        if (rank <= LAST_RANKED_PLACE) return 70;
        return damage > 1 ? 70 : 10;
    }

    public static int quality(int rank, double damage, double topDamage, int eyesPlaced) {
        double ratio = topDamage <= 0 ? 0 : Math.min(1, damage / topDamage);
        return placementBase(rank, damage) + EYE_BONUS * eyesPlaced + (int) Math.round(100 * ratio);
    }

    public static int enderPearls(int rank, double damage) {
        if (rank <= TOP_PEARLS.length) return TOP_PEARLS[rank - 1];
        if (rank <= 18) return 12;
        if (rank <= LAST_RANKED_PLACE) return 10;
        return damage > 1 ? 8 : 5;
    }

    public static int enchantedEnderPearls(int rank) {
        if (rank <= TOP_ENCHANTED.length) return TOP_ENCHANTED[rank - 1];
        return rank <= 18 ? 1 : 0;
    }

    public static int fragments(int remainingQuality) {
        return Math.max(0, Math.min(MAX_FRAGMENTS, remainingQuality / FRAGMENT_COST));
    }
}
