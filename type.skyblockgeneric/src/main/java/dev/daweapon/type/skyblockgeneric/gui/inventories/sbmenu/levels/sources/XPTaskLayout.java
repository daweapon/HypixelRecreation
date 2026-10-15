package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

/**
 * Hypixel's placement rule for the Ways to Level Up menus: items sit in rows of at most seven,
 * each row centred on the middle column, with an even-sized row leaving that middle column empty.
 * The block of rows is then centred vertically inside the space it is given.
 */
public final class XPTaskLayout {
    public static final int MAX_PER_ROW = 7;

    private static final int[][] COLUMNS = {
            {},
            {4},
            {3, 5},
            {2, 4, 6},
            {2, 3, 5, 6},
            {2, 3, 4, 5, 6},
            {1, 2, 3, 5, 6, 7},
            {1, 2, 3, 4, 5, 6, 7},
    };

    private XPTaskLayout() {
    }

    public static int capacity(int bandRows) {
        return bandRows * MAX_PER_ROW;
    }

    /** Inventory slots for {@code count} items placed inside the row band starting at {@code bandStartRow}. */
    public static int[] slots(int count, int bandStartRow, int bandRows) {
        if (count <= 0) return new int[0];

        int capped = Math.min(count, capacity(bandRows));
        int rows = Math.min(bandRows, (capped + MAX_PER_ROW - 1) / MAX_PER_ROW);
        int startRow = bandStartRow + Math.max(0, (bandRows - rows) / 2);

        int perRow = capped / rows;
        int withExtra = capped % rows;

        int[] slots = new int[capped];
        int written = 0;
        for (int row = 0; row < rows; row++) {
            int inRow = perRow + (row < withExtra ? 1 : 0);
            for (int column : COLUMNS[inRow]) {
                slots[written++] = (startRow + row) * 9 + column;
            }
        }
        return slots;
    }
}
