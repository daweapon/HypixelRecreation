package net.swofty.type.skyblockgeneric.garden.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolkitDataTest {
    @Test
    void storedToolsAndUnlockSurviveJson() {
        ToolkitData data = new ToolkitData();
        data.setUnlocked(true);
        data.store("PUMPKIN_DICER", "serialized");
        ToolkitData loaded = ToolkitData.fromJson(data.toJson());
        assertTrue(loaded.isUnlocked());
        assertEquals("serialized", loaded.stored("PUMPKIN_DICER"));
        assertNull(loaded.stored("MELON_DICER"));
    }

    @Test
    void takingAToolEmptiesItsSlot() {
        ToolkitData data = new ToolkitData();
        data.store("MELON_DICER", "x");
        assertEquals("x", data.take("MELON_DICER"));
        assertNull(data.stored("MELON_DICER"));
        assertFalse(data.isUnlocked());
    }

    @Test
    void everyToolFamilyHasExactlyOneToolkitSlot() {
        assertEquals(12, FarmingToolkit.SLOTS.size());
        assertEquals(12, FarmingToolkit.SLOTS.stream().map(FarmingToolkit.Slot::guiSlot).distinct().count());
        assertEquals("Pumpkin Dicer", FarmingToolkit.slotOf("PUMPKIN_DICER_2").name());
        assertNull(FarmingToolkit.slotOf("BASIC_GARDENING_HOE"));
        for (FarmingToolkit.Slot slot : FarmingToolkit.SLOTS) {
            assertEquals(slot, FarmingToolkit.slotOf(slot.family() + (slot.family().startsWith("THEORETICAL") ? "_1" : "")));
        }
    }
}
