package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToyBoxGridTest {
    @Test
    void everyGridSlotIsInsideTheBorderedInnerArea() {
        Set<Integer> seen = new HashSet<>();
        for (int index = 0; index < ToyBoxGrid.CAPACITY; index++) {
            int slot = ToyBoxGrid.slot(index);
            int row = slot / 9;
            int column = slot % 9;
            assertTrue(row >= 1 && row <= ToyBoxGrid.ROWS, "row of slot " + slot);
            assertTrue(column >= 1 && column <= ToyBoxGrid.COLUMNS, "column of slot " + slot);
            assertTrue(seen.add(slot), "duplicate slot " + slot);
        }
        assertEquals(28, seen.size());
    }

    @Test
    void gridFillsLeftToRightThenDown() {
        assertEquals(10, ToyBoxGrid.slot(0));
        assertEquals(16, ToyBoxGrid.slot(6));
        assertEquals(19, ToyBoxGrid.slot(7));
        assertEquals(43, ToyBoxGrid.slot(27));
        assertThrows(IndexOutOfBoundsException.class, () -> ToyBoxGrid.slot(28));
    }

    @Test
    void mainMenuHoldsTheGeneralRowAndEveryCategoryButton() {
        assertEquals(ToyBoxGrid.COLUMNS, ToyBoxButtons.general().size());
        assertTrue(ToyBoxCategory.values().length <= ToyBoxGrid.COLUMNS);
        assertTrue(ToyBoxButtons.uncategorised().size() <= ToyBoxGrid.CAPACITY - ToyBoxGrid.COLUMNS * 2);
    }

    @Test
    void everyCategoryFitsAndHasAtLeastOneButton() {
        for (ToyBoxCategory category : ToyBoxCategory.values()) {
            int size = category.buttons().size();
            assertTrue(size >= 1 && size <= ToyBoxGrid.CAPACITY, category + " has " + size + " buttons");
        }
    }

    @Test
    void noButtonNameIsRepeatedWithinAMenu() {
        Set<String> names = new HashSet<>();
        ToyBoxButtons.general().forEach(button -> assertTrue(names.add(button.name()), button.name()));
        for (ToyBoxCategory category : ToyBoxCategory.values()) {
            Set<String> inCategory = new HashSet<>();
            category.buttons().forEach(button -> assertTrue(inCategory.add(button.name()), category + ": " + button.name()));
        }
        assertFalse(names.isEmpty());
    }
}
