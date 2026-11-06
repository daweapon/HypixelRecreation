package dev.daweapon.dungeons.catacombs.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DungeonChestTierTest {
    @Test
    void baseCostsFollowTheFloorLootPages() {
        assertEquals(0, DungeonChestTier.WOOD.baseCost(CatacombsFloor.FLOOR_SEVEN));
        assertEquals(20_000, DungeonChestTier.GOLD.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(40_000, DungeonChestTier.DIAMOND.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(80_000, DungeonChestTier.EMERALD.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(200_000, DungeonChestTier.OBSIDIAN.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(50_000, DungeonChestTier.GOLD.baseCost(CatacombsFloor.FLOOR_TWO));
        assertEquals(200_000, DungeonChestTier.EMERALD.baseCost(CatacombsFloor.FLOOR_TWO));
        assertEquals(100_000, DungeonChestTier.GOLD.baseCost(CatacombsFloor.FLOOR_THREE));
        assertEquals(1_000_000, DungeonChestTier.OBSIDIAN.baseCost(CatacombsFloor.FLOOR_SEVEN));
        assertEquals(2_000_000, DungeonChestTier.BEDROCK.baseCost(CatacombsFloor.FLOOR_FIVE));
    }

    @Test
    void baseQualities() {
        assertEquals(110, DungeonChestTier.WOOD.baseQuality(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertEquals(300, DungeonChestTier.OBSIDIAN.baseQuality(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertEquals(370, DungeonChestTier.BEDROCK.baseQuality(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL));
        assertEquals(420, DungeonChestTier.BEDROCK.baseQuality(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER));
    }

    @Test
    void qualityFormulaMatchesTheWikiMaxBonusColumn() {
        assertEquals(110, DungeonChestRules.quality(110, false, 1.0, 0));
        assertEquals(168, DungeonChestRules.quality(110, true, 1.03, 23));
        assertEquals(225, DungeonChestRules.quality(160, true, 1.03, 23));
        assertEquals(269, DungeonChestRules.quality(200, true, 1.03, 23));
    }

    @Test
    void entranceHasNoChests() {
        assertTrue(DungeonChestRules.availableTiers(DungeonScoreRank.S_PLUS, CatacombsFloor.ENTRANCE).isEmpty());
    }

    @Test
    void floorSevenCompletionGrantsEmeraldAndObsidian() {
        assertEquals(List.of(DungeonChestTier.WOOD, DungeonChestTier.GOLD, DungeonChestTier.DIAMOND,
                        DungeonChestTier.EMERALD, DungeonChestTier.OBSIDIAN),
                DungeonChestRules.availableTiers(DungeonScoreRank.D, CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL));
        assertEquals(6, DungeonChestRules.availableTiers(DungeonScoreRank.D, CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER).size());
    }

    @Test
    void emeraldNeedsA() {
        assertFalse(DungeonChestTier.EMERALD.unlockedBy(DungeonScoreRank.B, CatacombsFloor.FLOOR_ONE));
        assertTrue(DungeonChestTier.EMERALD.unlockedBy(DungeonScoreRank.A, CatacombsFloor.FLOOR_ONE));
    }

    @Test
    void obsidianNeedsS() {
        assertFalse(DungeonChestTier.OBSIDIAN.unlockedBy(DungeonScoreRank.A, CatacombsFloor.FLOOR_ONE));
        assertTrue(DungeonChestTier.OBSIDIAN.unlockedBy(DungeonScoreRank.S, CatacombsFloor.FLOOR_ONE));
    }

    @Test
    void bedrockNeedsSPlusOnFloorFiveOrHigher() {
        assertFalse(DungeonChestTier.BEDROCK.unlockedBy(DungeonScoreRank.S, CatacombsFloor.FLOOR_SEVEN));
        assertFalse(DungeonChestTier.BEDROCK.unlockedBy(DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_FOUR));
        assertTrue(DungeonChestTier.BEDROCK.unlockedBy(DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_FIVE));
        assertTrue(DungeonChestTier.BEDROCK.unlockedBy(DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_SEVEN));
    }

    @Test
    void availableTiersByRank() {
        assertEquals(List.of(DungeonChestTier.WOOD, DungeonChestTier.GOLD, DungeonChestTier.DIAMOND),
                DungeonChestRules.availableTiers(DungeonScoreRank.D, CatacombsFloor.FLOOR_SIX));
        assertEquals(DungeonChestTier.values().length,
                DungeonChestRules.availableTiers(DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_SEVEN).size());
        assertEquals(DungeonChestTier.values().length - 1,
                DungeonChestRules.availableTiers(DungeonScoreRank.S_PLUS, CatacombsFloor.FLOOR_ONE).size());
    }

    @Test
    void claimRules() {
        assertEquals(1, DungeonChestRules.claimsPerRun(false));
        assertEquals(2, DungeonChestRules.claimsPerRun(true));
        assertEquals(3, DungeonChestRules.CHESTS_OFFERED);
        assertEquals(6, DungeonChestRules.MAXIMUM_CHESTS);
        assertEquals(1, DungeonChestRules.KISMET_FEATHER_REROLLS);
    }
}
