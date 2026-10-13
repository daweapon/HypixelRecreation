package net.swofty.type.skyblockgeneric.garden;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenCleaningTest {
    @Test
    void groundLevelBlocksNeverCount() {
        assertFalse(GardenCleaning.isCleanable("grass_block", 70));
        assertFalse(GardenCleaning.isCleanable("dirt", 64));
        assertTrue(GardenCleaning.isCleanable("grass_block", 71));
        assertTrue(GardenCleaning.isCleanable("stone", 75));
    }

    @Test
    void naturalGrowthCountsButBuiltBlocksDoNot() {
        for (String name : new String[]{"oak_log", "birch_leaves", "short_grass", "tall_grass", "poppy", "sunflower", "large_fern", "red_mushroom"}) {
            assertTrue(GardenCleaning.isCleanable(name, 72), name);
        }
        for (String name : new String[]{"oak_planks", "stone_bricks", "water", "farmland", "wheat", "air"}) {
            assertFalse(GardenCleaning.isCleanable(name, 72), name);
        }
    }

    @Test
    void scytheOnlyTakesGrassLeavesAndFlowers() {
        assertTrue(GardenCleaning.isScytheTarget("oak_leaves"));
        assertTrue(GardenCleaning.isScytheTarget("dandelion"));
        assertTrue(GardenCleaning.isScytheTarget("short_grass"));
        assertFalse(GardenCleaning.isScytheTarget("oak_log"));
        assertFalse(GardenCleaning.isScytheTarget("stone"));
    }

    @Test
    void progressAndThresholds() {
        assertEquals(0, GardenCleaning.progress(1000, 1000));
        assertEquals(0.5, GardenCleaning.progress(1000, 500));
        assertEquals(1, GardenCleaning.progress(0, 0));
        assertFalse(GardenCleaning.leavesDecay(1000, 300));
        assertTrue(GardenCleaning.leavesDecay(1000, 299));
        assertFalse(GardenCleaning.isClean(1000, 21));
        assertTrue(GardenCleaning.isClean(1000, 20));
    }
}
