package dev.daweapon.dungeons.catacombs.reward;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DungeonChestTierTest {
    @Test
    void coinCosts() {
        assertEquals(0, DungeonChestTier.WOOD.coinCost());
        assertEquals(25_000, DungeonChestTier.GOLD.coinCost());
        assertEquals(50_000, DungeonChestTier.DIAMOND.coinCost());
        assertEquals(100_000, DungeonChestTier.EMERALD.coinCost());
        assertEquals(250_000, DungeonChestTier.OBSIDIAN.coinCost());
        assertEquals(2_000_000, DungeonChestTier.BEDROCK.coinCost());
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
                DungeonChestRules.availableTiers(DungeonScoreRank.D, CatacombsFloor.FLOOR_SEVEN));
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
