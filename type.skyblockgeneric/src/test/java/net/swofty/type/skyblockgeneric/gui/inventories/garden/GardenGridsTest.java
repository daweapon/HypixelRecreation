package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenGridsTest {
    @Test
    void everyPlotHasOneDistinctSlotInsideTheChest() {
        Set<Integer> slots = new HashSet<>();
        for (int plot = 1; plot <= 24; plot++) {
            int slot = GardenGrids.slotOfPlot(plot);
            assertTrue(slot >= 0 && slot < 45, "plot " + plot);
            assertTrue(slots.add(slot), "plot " + plot);
            assertEquals(plot, GardenGrids.plotAt(slot).getAsInt());
        }
        assertFalse(GardenGrids.plotAt(GardenGrids.BARN_SLOT).isPresent());
        assertFalse(slots.contains(GardenGrids.BARN_SLOT));
    }

    @Test
    void plotPositionsMatchTheVideo() {
        assertEquals(2, GardenGrids.slotOfPlot(21));
        assertEquals(14, GardenGrids.slotOfPlot(6));
        assertEquals(33, GardenGrids.slotOfPlot(18));
        assertEquals(38, GardenGrids.slotOfPlot(23));
    }

    @Test
    void cropAndPresetSlotsDoNotCollide() {
        assertEquals(13, GardenGrids.CROP_ORDER.size());
        assertEquals(13, new HashSet<>(GardenGrids.CROP_SLOTS).size());
        Set<Integer> presets = new HashSet<>();
        GardenGrids.PRESETS.forEach(preset -> assertTrue(presets.add(preset.slot())));
        assertTrue(presets.stream().allMatch(slot -> slot < 45));
    }

    @Test
    void bundleCountOnlyForWholeBundles() {
        assertEquals(0, GardenGrids.bundleCount(128, 160));
        assertEquals(1, GardenGrids.bundleCount(160, 160));
        assertEquals(15, GardenGrids.bundleCount(2400, 160));
        assertEquals(0, GardenGrids.bundleCount(200, 160));
    }

    @Test
    void speedCapParsing() {
        assertEquals(155, GardenGrids.parseSpeedCap(" 155 ").getAsInt());
        assertEquals(1200, GardenGrids.parseSpeedCap("1,200").getAsInt());
        assertFalse(GardenGrids.parseSpeedCap("0").isPresent());
        assertFalse(GardenGrids.parseSpeedCap("abc").isPresent());
        assertFalse(GardenGrids.parseSpeedCap(null).isPresent());
        assertFalse(GardenGrids.parseSpeedCap("2000000").isPresent());
    }
}
