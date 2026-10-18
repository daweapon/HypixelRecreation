package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TabLayoutTest {

    private static List<TabLine> lines(String prefix, int count) {
        List<TabLine> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) lines.add(TabLine.of(Text.literal(prefix + i)));
        return lines;
    }

    private static TabLayout.Block block(String prefix, int count, boolean spacing, boolean wrapping) {
        return new TabLayout.Block(lines(prefix, count), spacing, wrapping);
    }

    private static String plain(TabLine line) {
        return line == null ? null : line.text().plain(Locale.ROOT);
    }

    @Test
    void capacityFollowsColumnCount() {
        assertEquals(80, TabLayout.layout(List.of(), 4).size());
        assertEquals(60, TabLayout.layout(List.of(), 3).size());
    }

    @Test
    void blocksFlowBackToBackColumnMajor() {
        List<TabLine> slots = TabLayout.layout(List.of(block("a", 3, false, false), block("b", 2, false, false)), 4);
        assertEquals("a0", plain(slots.get(0)));
        assertEquals("b0", plain(slots.get(3)));
        assertNull(slots.get(5));
    }

    @Test
    void spacingAddsBlankLineAfterBlock() {
        List<TabLine> slots = TabLayout.layout(List.of(block("a", 2, true, false), block("b", 1, false, false)), 4);
        assertNull(slots.get(2));
        assertEquals("b0", plain(slots.get(3)));
    }

    @Test
    void nonWrappingBlockMovesToNextColumnWhenItDoesNotFit() {
        List<TabLine> slots = TabLayout.layout(List.of(block("a", 15, false, false), block("b", 8, false, false)), 4);
        assertNull(slots.get(15));
        assertEquals("b0", plain(slots.get(20)));
        assertEquals("b7", plain(slots.get(27)));
    }

    @Test
    void wrappingBlockSplitsAcrossColumns() {
        List<TabLine> slots = TabLayout.layout(List.of(block("a", 15, false, false), block("b", 8, false, true)), 4);
        assertEquals("b0", plain(slots.get(15)));
        assertEquals("b4", plain(slots.get(19)));
        assertEquals("b5", plain(slots.get(20)));
    }

    @Test
    void blockTallerThanAColumnIsHidden() {
        List<TabLine> slots = TabLayout.layout(List.of(block("a", 21, false, false), block("b", 1, false, false)), 4);
        assertEquals("b0", plain(slots.get(0)));
    }

    @Test
    void overflowBeyondCapacityIsDropped() {
        List<TabLine> slots = TabLayout.layout(List.of(
                block("a", 19, false, false), block("b", 19, false, false), block("c", 19, false, false)), 2);
        assertEquals(40, slots.size());
        assertEquals("b18", plain(slots.get(38)));
        assertNull(slots.get(39));
    }
}
