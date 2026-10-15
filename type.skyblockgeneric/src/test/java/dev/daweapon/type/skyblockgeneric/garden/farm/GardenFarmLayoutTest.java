package dev.daweapon.type.skyblockgeneric.garden.farm;

import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenFarmLayoutTest {
    @Test
    void marginIsPlainGrass() {
        for (GardenCrop crop : GardenCrop.values()) {
            assertEquals(1, GardenFarmLayout.column(crop, 0, 50).size(), crop.name());
            assertEquals(Block.GRASS_BLOCK, GardenFarmLayout.column(crop, 95, 95).get(0), crop.name());
        }
    }

    @Test
    void everyCropPlantsSomethingInsideTheFarm() {
        for (GardenCrop crop : GardenCrop.values()) {
            boolean planted = false;
            for (int lx = 3; lx < 15 && !planted; lx++) {
                for (int lz = 3; lz < 15 && !planted; lz++) {
                    planted = GardenFarmLayout.column(crop, lx, lz).size() > 1;
                }
            }
            assertTrue(planted, crop.name());
        }
    }

    @Test
    void wheatRowsAreSeparatedByWater() {
        assertEquals(Block.WATER, GardenFarmLayout.column(GardenCrop.WHEAT, 7, 10).get(0));
        assertEquals(2, GardenFarmLayout.column(GardenCrop.WHEAT, 8, 10).size());
    }

    @Test
    void presetNamesMapToCrops() {
        assertEquals(GardenCrop.SUGAR_CANE, GardenPresets.cropOf("Sugar Cane"));
        assertNull(GardenPresets.cropOf("MEGA FARM MIDDLE"));
        assertTrue(GardenPresets.isPastable("Default"));
        assertFalse(GardenPresets.isPastable("NEON FARM CORNER"));
    }
}
