package dev.daweapon.dungeons.catacombs.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;

/**
 * The six post-boss chests (wiki Dungeon Reward Chest). Base costs come from the per-floor loot pages, except Floor
 * I's, which two captured runs showed at 20,000 / 40,000 / 80,000 coins (the wiki says 25,000 / 50,000 / 100,000);
 * Obsidian is scaled the same way. Base qualities come from the Chest Quality table.
 */
public enum DungeonChestTier {
    WOOD("Wood", "<f>", DungeonScoreRank.D, CatacombsFloor.FLOOR_ONE,
            new long[]{0, 0, 0},
            new int[]{110, 120, 125, 125, 125, 125, 125}, new int[]{120, 130, 135, 135, 140, 140, 140}),
    GOLD("Gold", "<6>", DungeonScoreRank.D, CatacombsFloor.FLOOR_ONE,
            new long[]{20_000, 50_000, 100_000},
            new int[]{160, 170, 180, 180, 180, 180, 180}, new int[]{170, 180, 195, 195, 195, 200, 200}),
    DIAMOND("Diamond", "<b>", DungeonScoreRank.D, CatacombsFloor.FLOOR_ONE,
            new long[]{40_000, 100_000, 250_000},
            new int[]{200, 210, 220, 220, 220, 220, 220}, new int[]{215, 225, 235, 240, 240, 245, 245}),
    EMERALD("Emerald", "<2>", DungeonScoreRank.A, CatacombsFloor.FLOOR_ONE,
            new long[]{80_000, 200_000, 500_000},
            new int[]{240, 250, 260, 260, 260, 260, 260}, new int[]{255, 265, 280, 280, 280, 290, 290}),
    OBSIDIAN("Obsidian", "<5>", DungeonScoreRank.S, CatacombsFloor.FLOOR_ONE,
            new long[]{200_000, 500_000, 1_000_000},
            new int[]{300, 320, 330, 330, 330, 330, 330}, new int[]{320, 340, 350, 355, 355, 365, 370}),
    BEDROCK("Bedrock", "<8>", DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_FIVE,
            new long[]{2_000_000, 2_000_000, 2_000_000},
            new int[]{0, 0, 0, 0, 370, 370, 370}, new int[]{0, 0, 0, 0, 405, 410, 420});

    private final String displayName;
    private final String colour;
    private final DungeonScoreRank requiredRank;
    private final CatacombsFloor minimumFloor;
    private final long[] baseCosts;
    private final int[] normalQuality;
    private final int[] masterQuality;

    DungeonChestTier(String displayName, String colour, DungeonScoreRank requiredRank, CatacombsFloor minimumFloor,
                     long[] baseCosts, int[] normalQuality, int[] masterQuality) {
        this.displayName = displayName;
        this.colour = colour;
        this.requiredRank = requiredRank;
        this.minimumFloor = minimumFloor;
        this.baseCosts = baseCosts;
        this.normalQuality = normalQuality;
        this.masterQuality = masterQuality;
    }

    public String displayName() {
        return displayName;
    }

    public String chestName() {
        return displayName + " Chest";
    }

    public String colour() {
        return colour;
    }

    public DungeonScoreRank requiredRank() {
        return requiredRank;
    }

    public CatacombsFloor minimumFloor() {
        return minimumFloor;
    }

    /** Floor I, Floor II, and Floors III-VII each have their own base price; Master Mode costs the same as Normal. */
    public long baseCost(CatacombsFloor floor) {
        if (floor == CatacombsFloor.ENTRANCE) return 0;
        return baseCosts[Math.min(floor.ordinal(), baseCosts.length) - 1];
    }

    public int baseQuality(CatacombsFloor floor, CatacombsMode mode) {
        if (floor == CatacombsFloor.ENTRANCE) return 0;
        int[] table = mode == CatacombsMode.MASTER ? masterQuality : normalQuality;
        return table[floor.ordinal() - 1];
    }

    public boolean unlockedBy(DungeonScoreRank rank, CatacombsFloor floor) {
        return rank.atLeast(requiredRank) && floor.ordinal() >= minimumFloor.ordinal();
    }
}
