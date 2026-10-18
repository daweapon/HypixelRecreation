package dev.daweapon.type.skyblockgeneric.tab;

import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TabLayoutTest {
    private static final Text HEADER = Text.literal("Info");

    private static List<Text> lines(String prefix, int count) {
        List<Text> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) lines.add(Text.literal(prefix + i));
        return lines;
    }

    private static String plain(Text text) {
        return text == null ? null : text.plain(Locale.ROOT);
    }

    @Test
    void alwaysProducesSixtySlotsStartingWithInfoHeader() {
        List<Text> slots = TabLayout.layout(HEADER, List.of());
        assertEquals(60, slots.size());
        assertEquals("Info", plain(slots.get(0)));
        assertNull(slots.get(1));
    }

    @Test
    void widgetsFlowBackToBackColumnMajor() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 3), false, false),
                new TabLayout.Block(lines("b", 2), false, false)));
        assertEquals("a0", plain(slots.get(1)));
        assertEquals("a2", plain(slots.get(3)));
        assertEquals("b0", plain(slots.get(4)));
    }

    @Test
    void spacingAddsBlankLineAfterWidget() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 2), true, false),
                new TabLayout.Block(lines("b", 1), false, false)));
        assertNull(slots.get(3));
        assertEquals("b0", plain(slots.get(4)));
    }

    @Test
    void nonWrappingWidgetMovesToNextColumnWhenItDoesNotFit() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 15), false, false),
                new TabLayout.Block(lines("b", 8), false, false)));
        assertNull(slots.get(16));
        assertEquals("b0", plain(slots.get(20)));
        assertEquals("b7", plain(slots.get(27)));
    }

    @Test
    void wrappingWidgetSplitsAcrossColumns() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 15), false, false),
                new TabLayout.Block(lines("b", 8), false, true)));
        assertEquals("b0", plain(slots.get(16)));
        assertEquals("b3", plain(slots.get(19)));
        assertEquals("b4", plain(slots.get(20)));
    }

    @Test
    void widgetThatCannotFitAnywhereIsHidden() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 21), false, false),
                new TabLayout.Block(lines("b", 1), false, false)));
        assertEquals("b0", plain(slots.get(1)));
    }

    @Test
    void overflowBeyondSixtySlotsIsDropped() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 19), false, false),
                new TabLayout.Block(lines("b", 19), false, false),
                new TabLayout.Block(lines("c", 19), false, false),
                new TabLayout.Block(lines("d", 19), false, false)));
        assertEquals(60, slots.size());
        assertEquals("c18", plain(slots.get(58)));
        assertNull(slots.get(59));
    }

    @Test
    void columnOfReturnsTwentyRows() {
        List<Text> slots = TabLayout.layout(HEADER, List.of());
        assertEquals(20, TabLayout.columnOf(slots, 2).size());
    }

    @Test
    void fewerColumnsShrinkCapacity() {
        List<Text> slots = TabLayout.layout(HEADER, List.of(
                new TabLayout.Block(lines("a", 19), false, false),
                new TabLayout.Block(lines("b", 19), false, false),
                new TabLayout.Block(lines("c", 19), false, false)), 2);
        assertEquals(40, slots.size());
        assertEquals("b18", plain(slots.get(38)));
    }
}
