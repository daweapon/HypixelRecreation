package dev.daweapon.service.darkauction.loot;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DarkAuctionItemPoolTest {
    @Test
    void newPoolHasNineDistinctItems() {
        DarkAuctionItemPool pool = new DarkAuctionItemPool();
        List<ItemType> available = pool.getAvailableItems();

        assertEquals(9, available.size());
        assertEquals(9, new HashSet<>(available).size());
        assertFalse(pool.isEmpty());
        assertFalse(available.contains(ItemType.DARK_PURPLE_DYE));
    }

    @Test
    void selectAndRemoveNeverRepeatsARegularItem() {
        DarkAuctionItemPool pool = new DarkAuctionItemPool();
        Set<ItemType> seen = new HashSet<>();

        for (int i = 0; i < 9; i++) {
            ItemType picked = pool.selectAndRemove();
            if (picked == ItemType.DARK_PURPLE_DYE) continue;
            assertTrue(seen.add(picked), "duplicate " + picked);
        }
    }

    @Test
    void selectedItemIsRemovedFromAvailableList() {
        DarkAuctionItemPool pool = new DarkAuctionItemPool();
        List<ItemType> before = new ArrayList<>(pool.getAvailableItems());

        ItemType picked = pool.selectAndRemove();

        if (picked == ItemType.DARK_PURPLE_DYE) {
            assertEquals(before, pool.getAvailableItems());
        } else {
            assertTrue(before.contains(picked));
            assertEquals(before.size() - 1, pool.getAvailableItems().size());
            assertFalse(pool.getAvailableItems().contains(picked));
        }
    }

    @Test
    void exhaustedPoolFallsBackToMidasSword() {
        DarkAuctionItemPool pool = new DarkAuctionItemPool();
        while (!pool.isEmpty()) {
            pool.selectAndRemove();
        }

        ItemType fallback = pool.selectAndRemove();

        assertTrue(fallback == ItemType.MIDAS_SWORD || fallback == ItemType.DARK_PURPLE_DYE);
        assertTrue(pool.isEmpty());
    }

    @Test
    void bookPoolAlwaysReturnsEnchantedBook() {
        for (int i = 0; i < 20; i++) {
            assertEquals(ItemType.ENCHANTED_BOOK, DarkAuctionBookPool.selectRandom());
        }
    }

    @Test
    void randomBookTypeNameIsAlwaysFromAvailableList() {
        List<String> available = DarkAuctionBookPool.getAvailableBookTypes();
        assertEquals(9, available.size());
        for (int i = 0; i < 50; i++) {
            assertTrue(available.contains(DarkAuctionBookPool.getRandomBookTypeName()));
        }
    }
}
