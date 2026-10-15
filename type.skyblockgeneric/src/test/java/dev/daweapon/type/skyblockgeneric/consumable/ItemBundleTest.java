package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemBundleTest {
    @Test
    void bundlesMatchTheirTooltips() {
        assertEquals(9, ItemBundle.ECCENTRIC_PAINTING_BUNDLE.count());
        assertEquals("ECCENTRIC_PAINTING", ItemBundle.ECCENTRIC_PAINTING_BUNDLE.itemId());
        assertNull(ItemBundle.ECCENTRIC_PAINTING_BUNDLE.enchantment());
        assertEquals(EnchantmentType.ICE_COLD, ItemBundle.ENCHANTED_BOOK_BUNDLE_ICE_COLD.enchantment());
        assertEquals(5, ItemBundle.ENCHANTED_BOOK_BUNDLE_ICE_COLD.level());
        assertEquals(4, ItemBundle.ENCHANTED_BOOK_BUNDLE_ICE_COLD.count());
        assertEquals(7, ItemBundle.ENCHANTED_BOOK_BUNDLE_POWER.level());
        assertEquals(5, ItemBundle.ENCHANTED_BOOK_BUNDLE_BIG_BRAIN.count());
        assertEquals(5, ItemBundle.ENCHANTED_BOOK_BUNDLE_VICIOUS.count());
        assertEquals(3, ItemBundle.ENCHANTED_BOOK_BUNDLE_REFLECTION.count());
    }

    @Test
    void everyBundleHasAnEnchantmentOrAnItem() {
        for (ItemBundle bundle : ItemBundle.values()) {
            assertNotNull(bundle.enchantment() != null ? bundle.enchantment() : bundle.itemId(), bundle.name());
        }
    }

    @Test
    void dwarvenOsGiveTwoFortuneEachUpToFive() {
        assertEquals(0, DwarvenOs.fortune(0));
        assertEquals(6, DwarvenOs.fortune(3), 1e-9);
        assertEquals(10, DwarvenOs.fortune(5), 1e-9);
        assertEquals(10, DwarvenOs.fortune(99), 1e-9);
    }

    @Test
    void dwarvenOsMapToTheirFortuneStatistic() {
        ItemStatistics stats = DwarvenOs.statistics(new int[]{5, 0, 1, 2});
        assertEquals(10, stats.getOverall(ItemStatistic.ORE_FORTUNE), 1e-9);
        assertEquals(0, stats.getOverall(ItemStatistic.BLOCK_FORTUNE), 1e-9);
        assertEquals(2, stats.getOverall(ItemStatistic.GEMSTONE_FORTUNE), 1e-9);
        assertEquals(4, stats.getOverall(ItemStatistic.DWARVEN_METAL_FORTUNE), 1e-9);
    }
}
