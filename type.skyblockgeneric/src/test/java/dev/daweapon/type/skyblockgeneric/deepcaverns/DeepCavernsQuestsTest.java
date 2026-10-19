package dev.daweapon.type.skyblockgeneric.deepcaverns;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepCavernsQuestsTest {
    @Test
    void rhysAcceptsAnyMixOfTheSevenOresUpToFifteen() {
        Map<ItemType, Integer> have = Map.of(ItemType.ENCHANTED_COAL, 5, ItemType.ENCHANTED_DIAMOND, 20, ItemType.COBBLESTONE, 64);
        Map<ItemType, Integer> plan = DeepCavernsQuests.planRhysHandIn(0, type -> have.getOrDefault(type, 0));
        assertEquals(Map.of(ItemType.ENCHANTED_COAL, 5, ItemType.ENCHANTED_DIAMOND, 10), plan);
    }

    @Test
    void rhysCountsPartialHandInsAndNeverTakesMoreThanIsNeeded() {
        Map<ItemType, Integer> plan = DeepCavernsQuests.planRhysHandIn(12, type -> type == ItemType.ENCHANTED_REDSTONE ? 64 : 0);
        assertEquals(Map.of(ItemType.ENCHANTED_REDSTONE, 3), plan);
        assertTrue(DeepCavernsQuests.planRhysHandIn(15, type -> 64).isEmpty());
    }

    @Test
    void rhysIgnoresEveryOtherItem() {
        assertTrue(DeepCavernsQuests.planRhysHandIn(0, type -> type == ItemType.COBBLESTONE ? 64 : 0).isEmpty());
        assertEquals(7, DeepCavernsQuests.RHYS_ORES.size());
    }

    @Test
    void collectionTiersCountTheReachedRequirements() {
        assertEquals(0, DeepCavernsQuests.collectionTier(99, 100, 250, 750));
        assertEquals(2, DeepCavernsQuests.collectionTier(250, 100, 250, 750));
        assertEquals(3, DeepCavernsQuests.collectionTier(5_000, 100, 250, 750));
    }

    @Test
    void redstoneMinerAsksForOneHundredTwentyEightDoublingToAThousandAndTwentyFour() {
        assertEquals(128, DeepCavernsQuests.redstoneCost(0));
        assertEquals(256, DeepCavernsQuests.redstoneCost(1));
        assertEquals(512, DeepCavernsQuests.redstoneCost(2));
        assertEquals(1024, DeepCavernsQuests.redstoneCost(3));
        int total = 0;
        for (int tier = 0; tier < 4; tier++) total += DeepCavernsQuests.redstoneCost(tier);
        assertEquals(1_920, total);
    }

    @Test
    void redstoneMinerGrantsOneSlotPerTierUpToFour() {
        assertEquals(0, DeepCavernsQuests.redstoneSlots(0));
        assertEquals(3, DeepCavernsQuests.redstoneSlots(3));
        assertEquals(4, DeepCavernsQuests.redstoneSlots(9));
        assertFalse(DeepCavernsQuests.redstoneFinished(3));
        assertTrue(DeepCavernsQuests.redstoneFinished(4));
    }
}
