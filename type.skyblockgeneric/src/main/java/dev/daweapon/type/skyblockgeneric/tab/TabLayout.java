package dev.daweapon.type.skyblockgeneric.tab;

import java.util.ArrayList;
import java.util.List;

public final class TabLayout {
    public static final int ROWS = 20;
    public static final int MAX_COLUMNS = 4;

    private TabLayout() {
    }

    public record Block(List<TabLine> lines, boolean spacing, boolean wrapping) {
        public Block(List<TabLine> lines) {
            this(lines, false, false);
        }
    }

    public static List<TabLine> layout(List<Block> blocks, int columns) {
        int capacity = ROWS * Math.max(0, Math.min(columns, MAX_COLUMNS));
        TabLine[] slots = new TabLine[capacity];
        int cursor = 0;

        for (Block block : blocks) {
            List<TabLine> lines = new ArrayList<>(block.lines());
            if (lines.isEmpty()) continue;
            if (block.spacing()) lines.add(null);

            int rowInColumn = cursor % ROWS;
            int remaining = ROWS - rowInColumn;
            if (lines.size() > remaining && !block.wrapping()) {
                if (lines.size() > ROWS) continue;
                if (rowInColumn != 0) cursor += remaining;
            }
            if (cursor + (block.wrapping() ? 1 : lines.size()) > capacity) continue;

            for (TabLine line : lines) {
                if (cursor >= capacity) break;
                slots[cursor++] = line;
            }
        }

        List<TabLine> result = new ArrayList<>(capacity);
        for (TabLine slot : slots) result.add(slot);
        return result;
    }
}
