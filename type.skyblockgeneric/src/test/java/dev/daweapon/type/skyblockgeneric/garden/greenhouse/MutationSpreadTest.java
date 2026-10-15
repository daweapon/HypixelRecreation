package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutationSpreadTest {
    private final MutationCatalogue catalogue = MutationCatalogue.get();

    private GreenhouseData.Bed bed(Map<Integer, String> plants) {
        GreenhouseData.Bed bed = new GreenhouseData.Bed();
        java.util.Arrays.fill(bed.getUnlocked(), true);
        plants.forEach((slot, type) -> bed.getPlants().put(slot, new GreenhouseData.Plant(type, 0)));
        return bed;
    }

    @Test
    void every_mutation_is_registered_as_a_plant_and_has_an_item() {
        assertEquals(40, catalogue.all().size());
        for (MutationCatalogue.Mutation mutation : catalogue.all()) {
            assertNotNull(GreenhouseConfig.get().plant(mutation.id()), mutation.id());
            assertNotNull(mutation.item(), "item " + mutation.itemId());
        }
    }

    @Test
    void dustgrainNeedsTwoWheatAroundTheSlot() {
        MutationCatalogue.Mutation dustgrain = catalogue.mutation("DUSTGRAIN");
        int centre = GreenhouseData.slot(5, 5);
        assertTrue(MutationSpread.eligible(dustgrain, bed(Map.of(GreenhouseData.slot(4, 5), "WHEAT", GreenhouseData.slot(6, 6), "WHEAT")), centre, Map.of()));
        assertFalse(MutationSpread.eligible(dustgrain, bed(Map.of(GreenhouseData.slot(4, 5), "WHEAT")), centre, Map.of()));
        assertFalse(MutationSpread.eligible(dustgrain, bed(Map.of(GreenhouseData.slot(4, 5), "CARROT", GreenhouseData.slot(6, 6), "CARROT")), centre, Map.of()));
    }

    @Test
    void diagonalNeighboursCount() {
        MutationCatalogue.Mutation scourroot = catalogue.mutation("SCOURROOT");
        int centre = GreenhouseData.slot(2, 2);
        assertTrue(MutationSpread.eligible(scourroot, bed(Map.of(GreenhouseData.slot(1, 1), "CARROT", GreenhouseData.slot(3, 3), "CARROT",
                GreenhouseData.slot(1, 3), "POTATO", GreenhouseData.slot(3, 1), "POTATO")), centre, Map.of()));
    }

    @Test
    void spreadingFillsEmptySlotsOverManyStages() {
        GreenhouseData.Bed bed = bed(Map.of(GreenhouseData.slot(4, 5), "WHEAT", GreenhouseData.slot(6, 6), "WHEAT"));
        Random random = new Random(3);
        boolean grew = false;
        for (int i = 0; i < 200 && !grew; i++) {
            grew = MutationSpread.roll(bed, 0, random).contains(GreenhouseData.slot(5, 5));
        }
        assertTrue(grew);
    }
}
