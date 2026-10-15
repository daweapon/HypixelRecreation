package dev.daweapon.dungeons.catacombs.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;

public enum DungeonChestTier {
    WOOD("Wood", 0, DungeonScoreRank.D, CatacombsFloor.ENTRANCE),
    GOLD("Gold", 25_000, DungeonScoreRank.D, CatacombsFloor.ENTRANCE),
    DIAMOND("Diamond", 50_000, DungeonScoreRank.D, CatacombsFloor.ENTRANCE),
    EMERALD("Emerald", 100_000, DungeonScoreRank.A, CatacombsFloor.ENTRANCE),
    OBSIDIAN("Obsidian", 250_000, DungeonScoreRank.S, CatacombsFloor.ENTRANCE),
    BEDROCK("Bedrock", 2_000_000, DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_FIVE);

    private final String displayName;
    private final long coinCost;
    private final DungeonScoreRank requiredRank;
    private final CatacombsFloor minimumFloor;

    DungeonChestTier(String displayName, long coinCost, DungeonScoreRank requiredRank, CatacombsFloor minimumFloor) {
        this.displayName = displayName;
        this.coinCost = coinCost;
        this.requiredRank = requiredRank;
        this.minimumFloor = minimumFloor;
    }

    public String displayName() {
        return displayName;
    }

    public long coinCost() {
        return coinCost;
    }

    public DungeonScoreRank requiredRank() {
        return requiredRank;
    }

    public CatacombsFloor minimumFloor() {
        return minimumFloor;
    }

    public boolean unlockedBy(DungeonScoreRank rank, CatacombsFloor floor) {
        return rank.atLeast(requiredRank) && floor.ordinal() >= minimumFloor.ordinal();
    }
}
