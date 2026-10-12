package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.inventory.InventoryType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarSetLayoutTest {
    @Test
    void smallSetsKeepCompactLayout() {
        for (int size = 1; size <= BazaarSetLayout.COMPACT_LIMIT; size++) {
            BazaarSetLayout layout = new BazaarSetLayout(size);
            assertFalse(layout.isPaged());
            assertEquals(1, layout.pageCount());
            assertEquals(size, layout.slots(0).length);
            assertEquals(InventoryType.CHEST_4_ROW, layout.inventoryType());
        }
        assertArrayEquals(new int[]{13}, new BazaarSetLayout(1).slots(0));
        assertArrayEquals(new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24}, new BazaarSetLayout(10).slots(0));
    }

    @Test
    void largeSetsPaginate() {
        BazaarSetLayout layout = new BazaarSetLayout(11);
        assertTrue(layout.isPaged());
        assertEquals(1, layout.pageCount());
        assertEquals(11, layout.slots(0).length);
        assertEquals(InventoryType.CHEST_6_ROW, layout.inventoryType());

        assertEquals(1, new BazaarSetLayout(28).pageCount());
        assertEquals(2, new BazaarSetLayout(29).pageCount());
        assertEquals(4, new BazaarSetLayout(85).pageCount());
    }

    @Test
    void pagesCoverEveryItemOnceWithoutSlotClashes() {
        int total = 85;
        BazaarSetLayout layout = new BazaarSetLayout(total);
        int covered = 0;
        for (int page = 0; page < layout.pageCount(); page++) {
            assertEquals(covered, layout.firstIndex(page));
            int[] slots = layout.slots(page);
            Set<Integer> unique = new HashSet<>(Arrays.stream(slots).boxed().toList());
            assertEquals(slots.length, unique.size());
            for (int slot : slots) {
                assertTrue(slot >= 10 && slot <= 43);
                assertFalse(Set.of(layout.closeSlot(), layout.backSlot(), layout.ordersSlot(),
                        layout.previousPageSlot(), layout.nextPageSlot()).contains(slot));
            }
            covered += slots.length;
        }
        assertEquals(total, covered);
    }

    @Test
    void pageIndexIsClamped() {
        BazaarSetLayout layout = new BazaarSetLayout(60);
        assertEquals(0, layout.clampPage(-3));
        assertEquals(2, layout.clampPage(99));
        assertEquals(4, layout.itemsOnPage(2));
    }

    @Test
    void emptySetIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BazaarSetLayout(0));
    }
}
