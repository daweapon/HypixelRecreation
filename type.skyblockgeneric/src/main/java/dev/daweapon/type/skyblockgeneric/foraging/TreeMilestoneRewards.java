package dev.daweapon.type.skyblockgeneric.foraging;

import java.util.Map;

public final class TreeMilestoneRewards {
    private static final Map<String, Integer> SWEEP_PER_TIER = Map.of("FIG", 1, "MANGROVE", 2, "HELIX", 3);
    private static final Map<String, long[]> FORAGING_XP = Map.of(
            "FIG", new long[]{2_000, 4_000, 6_000, 10_000, 15_000, 25_000, 50_000},
            "MANGROVE", new long[]{3_000, 6_000, 10_000, 15_000, 25_000, 50_000, 100_000},
            "HELIX", new long[]{5_000, 9_000, 15_000, 20_000, 35_000, 75_000, 150_000});

    private TreeMilestoneRewards() {
    }

    public static int sweepPerTier(String treeType) {
        return SWEEP_PER_TIER.getOrDefault(treeType, 0);
    }

    public static int sweep(String treeType, int milestonesReached) {
        return sweepPerTier(treeType) * Math.max(0, Math.min(milestonesReached, TreeGiftMilestones.TIER_GIFTS.length));
    }

    public static long foragingXp(String treeType, int tier) {
        long[] table = FORAGING_XP.get(treeType);
        return table == null || tier < 1 || tier > table.length ? 0 : table[tier - 1];
    }

    public static int totalSweep(Map<String, Integer> giftCounts) {
        int total = 0;
        for (String type : TreeGiftMilestones.TREE_TYPES) total += sweep(type, TreeGiftMilestones.milestones(giftCounts.getOrDefault(type, 0)));
        return total;
    }
}
