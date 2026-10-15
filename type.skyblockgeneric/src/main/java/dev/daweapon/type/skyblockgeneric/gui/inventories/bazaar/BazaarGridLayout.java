package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import dev.daweapon.type.skyblockgeneric.bazaar.BazaarItemSet;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarTile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BazaarGridLayout {
    public static final int[] TILE_SLOTS = {
            11, 12, 13, 14, 15, 16,
            20, 21, 22, 23, 24, 25,
            29, 30, 31, 32, 33, 34,
            38, 39, 40, 41, 42, 43
    };
    public static final int[] TAB_SLOTS = {0, 9, 18, 27, 36};
    public static final int[] PANE_SLOTS = {
            1, 2, 3, 4, 5, 6, 7, 8,
            10, 19, 28, 37, 46,
            17, 26, 35, 44, 53
    };
    public static final int SEARCH_SLOT = 45;
    public static final int SELL_INVENTORY_SLOT = 47;
    public static final int SELL_SACKS_SLOT = 48;
    public static final int CLOSE_SLOT = 49;
    public static final int ORDERS_SLOT = 50;
    public static final int HISTORY_SLOT = 51;
    public static final int SETTINGS_SLOT = 52;

    private BazaarGridLayout() {
    }

    public static int[] assign(List<BazaarTile> tiles) {
        int[] slots = new int[tiles.size()];
        Set<Integer> used = new HashSet<>();
        int next = 0;
        for (int i = 0; i < tiles.size(); i++) {
            int position = tiles.get(i).gridPosition();
            int index = position == BazaarItemSet.NO_POSITION ? next : position;
            if (index >= TILE_SLOTS.length) {
                throw new IllegalStateException("Tile " + tiles.get(i).name() + " does not fit the category grid");
            }
            if (!used.add(index)) {
                throw new IllegalStateException("Tile " + tiles.get(i).name() + " collides at grid index " + index);
            }
            slots[i] = TILE_SLOTS[index];
            next = index + 1;
        }
        return slots;
    }

    public static List<Integer> unusedTileSlots(int[] assigned) {
        Set<Integer> taken = new HashSet<>();
        for (int slot : assigned) {
            taken.add(slot);
        }
        List<Integer> free = new ArrayList<>();
        for (int slot : TILE_SLOTS) {
            if (!taken.contains(slot)) {
                free.add(slot);
            }
        }
        return free;
    }
}
