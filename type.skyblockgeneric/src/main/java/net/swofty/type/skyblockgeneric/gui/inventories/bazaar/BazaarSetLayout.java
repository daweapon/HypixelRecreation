package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.inventory.InventoryType;

import java.util.Arrays;
import java.util.Map;

public final class BazaarSetLayout {
    public static final int COMPACT_LIMIT = 10;
    public static final int PAGE_CAPACITY = 28;

    private static final Map<Integer, int[]> COMPACT_SLOTS = Map.of(
            1, new int[]{13},
            2, new int[]{12, 14},
            3, new int[]{11, 13, 15},
            4, new int[]{10, 12, 14, 16},
            5, new int[]{11, 12, 13, 14, 15},
            6, new int[]{11, 12, 13, 14, 15, 22},
            7, new int[]{10, 11, 12, 13, 14, 15, 16},
            8, new int[]{11, 12, 13, 14, 15, 21, 22, 23},
            9, new int[]{10, 11, 12, 13, 14, 15, 16, 21, 23},
            10, new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24}
    );

    private static final int[] PAGED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final int totalItems;

    public BazaarSetLayout(int totalItems) {
        if (totalItems < 1) {
            throw new IllegalArgumentException("A bazaar item set needs at least one item");
        }
        this.totalItems = totalItems;
    }

    public boolean isPaged() {
        return totalItems > COMPACT_LIMIT;
    }

    public int pageCount() {
        return isPaged() ? (totalItems + PAGE_CAPACITY - 1) / PAGE_CAPACITY : 1;
    }

    public int clampPage(int page) {
        return Math.max(0, Math.min(page, pageCount() - 1));
    }

    public int firstIndex(int page) {
        return isPaged() ? clampPage(page) * PAGE_CAPACITY : 0;
    }

    public int itemsOnPage(int page) {
        return isPaged() ? Math.min(PAGE_CAPACITY, totalItems - firstIndex(page)) : totalItems;
    }

    public int[] slots(int page) {
        int count = itemsOnPage(page);
        return isPaged() ? Arrays.copyOf(PAGED_SLOTS, count) : COMPACT_SLOTS.get(count);
    }

    public InventoryType inventoryType() {
        return isPaged() ? InventoryType.CHEST_6_ROW : InventoryType.CHEST_4_ROW;
    }

    public int closeSlot() {
        return isPaged() ? 49 : 31;
    }

    public int backSlot() {
        return isPaged() ? 48 : 30;
    }

    public int ordersSlot() {
        return isPaged() ? 50 : 32;
    }

    public int previousPageSlot() {
        return 45;
    }

    public int nextPageSlot() {
        return 53;
    }
}
