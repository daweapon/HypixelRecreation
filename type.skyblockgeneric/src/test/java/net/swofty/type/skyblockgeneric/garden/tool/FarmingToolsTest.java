package net.swofty.type.skyblockgeneric.garden.tool;

import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmingToolsTest {
    @Test
    void toolIdsMapToCropAndTier() {
        FarmingTools.Tool wheat = FarmingTools.of("THEORETICAL_HOE_WHEAT_2");
        assertEquals(GardenCrop.WHEAT, wheat.crop());
        assertEquals(2, wheat.mk());
        assertEquals(1, FarmingTools.of("PUMPKIN_DICER").mk());
        assertEquals(3, FarmingTools.of("MELON_DICER_3").mk());
        FarmingTools.Tool sunflower = FarmingTools.of("THEORETICAL_HOE_SUNFLOWER_1");
        assertTrue(sunflower.works(GardenCrop.MOONFLOWER));
        assertNull(FarmingTools.of("BASIC_GARDENING_HOE"));
    }

    @Test
    void levelsFollowTheWikiTableAndTierCaps() {
        assertEquals(1, FarmingTools.level(0, 1));
        assertEquals(2, FarmingTools.level(1_000, 1));
        assertEquals(5, FarmingTools.level(11_000, 1));
        assertEquals(14, FarmingTools.level(372_000, 1));
        assertEquals(14, FarmingTools.level(49_722_000, 1));
        assertEquals(29, FarmingTools.level(49_722_000, 2));
        assertEquals(40, FarmingTools.level(49_722_000, 3));
        assertEquals(16_922_000, FarmingTools.cumulativeXp(40));
    }

    @Test
    void xpPerCollectionMatchesTheDivisors() {
        assertEquals(2.5, FarmingTools.xpPerCollection(GardenCrop.WHEAT));
        assertEquals(1.0, FarmingTools.xpPerCollection(GardenCrop.POTATO) * 3, 1e-9);
    }
}
