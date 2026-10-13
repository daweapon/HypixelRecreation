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

    @Test
    void overclockersRaiseOnlyTheMkThreeCap() {
        assertEquals(40, FarmingTools.levelCap(3, 0));
        assertEquals(45, FarmingTools.levelCap(3, 5));
        assertEquals(50, FarmingTools.levelCap(3, 10));
        assertEquals(50, FarmingTools.levelCap(3, 25));
        assertEquals(14, FarmingTools.levelCap(1, 10));
        assertEquals(29, FarmingTools.levelCap(2, 10));
        assertEquals(45, FarmingTools.level(49_722_000, 3, 5));
        assertEquals(50, FarmingTools.level(49_722_000, 3, 10));
    }

    @Test
    void toolFamiliesStripTheTierSuffix() {
        assertEquals("PUMPKIN_DICER", FarmingTools.familyOf("PUMPKIN_DICER"));
        assertEquals("PUMPKIN_DICER", FarmingTools.familyOf("PUMPKIN_DICER_3"));
        assertEquals("THEORETICAL_HOE_WHEAT", FarmingTools.familyOf("THEORETICAL_HOE_WHEAT_2"));
        assertNull(FarmingTools.familyOf("BASIC_GARDENING_HOE"));
    }

    @Test
    void experienceBelowMaxLevelJustAccumulates() {
        FarmingTools.Gain gain = FarmingTools.gain(1_000, 0, 3, 10, 500, true);
        assertEquals(1_500, gain.xp());
        assertEquals(0, gain.overflow());
        assertEquals(0, gain.capsules());
    }

    @Test
    void maxLevelExperienceOverflowsIntoCapsules() {
        long max = FarmingTools.cumulativeXp(50);
        FarmingTools.Gain gain = FarmingTools.gain(max - 100, 150_000, 3, 10, 350_000, true);
        assertEquals(max, gain.xp());
        assertEquals(2, gain.capsules());
        assertEquals(99_900, gain.overflow());
    }

    @Test
    void overflowIsIgnoredBelowTheFullCap() {
        long max = FarmingTools.cumulativeXp(50);
        FarmingTools.Gain gain = FarmingTools.gain(max - 100, 0, 3, 9, 500_000, true);
        assertEquals(max + 499_900, gain.xp());
        assertEquals(0, gain.capsules());
    }

    @Test
    void capsulesNeverBankOverflow() {
        long max = FarmingTools.cumulativeXp(50);
        FarmingTools.Gain gain = FarmingTools.gain(max - 10, 0, 3, 10, FarmingTools.CAPSULE_XP, false);
        assertEquals(max, gain.xp());
        assertEquals(0, gain.capsules());
        assertEquals(0, gain.overflow());
    }
}
