package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class TabLayout {
    public static final int ROWS = 20;
    public static final int WIDGET_COLUMNS = 3;
    public static final int WIDGET_SLOTS = ROWS * WIDGET_COLUMNS;

    private TabLayout() {
    }

    public record Block(List<Text> lines, boolean spacing, boolean wrapping) {
    }

    public static List<Text> layout(Text infoHeader, List<Block> blocks) {
        return layout(infoHeader, blocks, WIDGET_COLUMNS);
    }

    public static List<Text> layout(Text infoHeader, List<Block> blocks, int columns) {
        int capacity = ROWS * columns;
        Text[] slots = new Text[capacity];
        int cursor = 0;
        if (capacity == 0) return new ArrayList<>();
        slots[cursor++] = infoHeader;

        for (Block block : blocks) {
            List<Text> lines = new ArrayList<>(block.lines());
            if (lines.isEmpty()) continue;
            if (block.spacing()) lines.add(null);

            int rowInColumn = cursor % ROWS;
            int remaining = ROWS - rowInColumn;
            if (lines.size() > remaining && !block.wrapping()) {
                if (lines.size() > ROWS) continue;
                if (rowInColumn != 0) cursor += remaining;
            }
            if (cursor + (block.wrapping() ? 1 : lines.size()) > capacity) continue;

            for (Text line : lines) {
                if (cursor >= capacity) break;
                slots[cursor++] = line;
            }
        }

        List<Text> result = new ArrayList<>(capacity);
        for (Text slot : slots) result.add(slot);
        return result;
    }

    public static List<Text> columnOf(List<Text> widgetSlots, int column) {
        return new ArrayList<>(widgetSlots.subList(column * ROWS, (column + 1) * ROWS));
    }
}
