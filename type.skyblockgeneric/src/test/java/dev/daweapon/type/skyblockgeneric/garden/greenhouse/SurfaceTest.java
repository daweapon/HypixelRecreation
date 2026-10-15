package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfaceTest {
    @Test
    void requirementsListAcceptedSurfaces() {
        assertEquals(List.of(Surface.SAND, Surface.DIRT), Surface.allowed("Sand/Dirt"));
        assertEquals(List.of(Surface.FARMLAND, Surface.DIRT), Surface.allowed("Farmland / Dirt"));
        assertTrue(Surface.accepts("Soul Sand", Surface.SOUL_SAND));
        assertFalse(Surface.accepts("Soul Sand", Surface.FARMLAND));
        assertTrue(Surface.accepts("Farmland / Dirt", Surface.DIRT));
        assertTrue(Surface.accepts(null, Surface.SAND));
    }

    @Test
    void blockItemsMapToSurfaces() {
        assertEquals(Surface.SOUL_SAND, Surface.ofItem("SOUL_SAND"));
        assertEquals(Surface.MYCELIUM, Surface.ofItem("MYCELIUM"));
        assertEquals(Surface.END_STONE, Surface.ofItem("END_STONE"));
        assertNull(Surface.ofItem("WHEAT"));
        assertNull(Surface.ofItem(null));
    }

    @Test
    void baseCropsKeepTheirRealSurfaces() {
        GreenhouseConfig config = GreenhouseConfig.get();
        assertEquals("Soul Sand", config.plant("NETHER_WART").surface());
        assertEquals("Mycelium", config.plant("MUSHROOM").surface());
        assertTrue(Surface.accepts(config.plant("SUGAR_CANE").surface(), Surface.SAND));
        assertTrue(Surface.accepts(config.plant("SUGAR_CANE").surface(), Surface.DIRT));
        assertFalse(Surface.accepts(config.plant("SUGAR_CANE").surface(), Surface.SOUL_SAND));
        assertTrue(Surface.accepts(config.plant("WHEAT").surface(), Surface.FARMLAND));
    }

    @Test
    void surfacesAndPlantStateSurviveJson() {
        GreenhouseData data = new GreenhouseData();
        GreenhouseData.Bed bed = new GreenhouseData.Bed();
        bed.setSurface(7, Surface.SOUL_SAND);
        bed.setSurface(8, Surface.FARMLAND);
        GreenhouseData.Plant plant = new GreenhouseData.Plant("SNOOZLING", 5);
        plant.setState("asleep", 1);
        bed.getPlants().put(20, plant);
        data.getBeds().put(24, bed);
        data.setFlasksConsumed(3);
        GreenhouseData read = GreenhouseData.fromJson(data.toJson());
        assertEquals(Surface.SOUL_SAND, read.bed(24).surface(7));
        assertEquals(Surface.FARMLAND, read.bed(24).surface(8));
        assertEquals(Surface.FARMLAND, read.bed(24).surface(0));
        assertEquals(1, read.bed(24).getPlants().get(20).state("asleep"));
        assertEquals(3, read.getFlasksConsumed());
    }

    @Test
    void oldSavesWithoutSurfacesStillLoad() {
        GreenhouseData data = new GreenhouseData();
        data.getBeds().put(24, new GreenhouseData.Bed());
        var json = data.toJson();
        json.getJSONObject("beds").getJSONObject("24").remove("surfaces");
        assertEquals(Surface.FARMLAND, GreenhouseData.fromJson(json).bed(24).surface(3));
    }
}
