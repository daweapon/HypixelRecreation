package net.swofty.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutationFootprintTest {
    private final MutationCatalogue catalogue = MutationCatalogue.get();

    private GreenhouseData.Bed bed() {
        GreenhouseData.Bed bed = new GreenhouseData.Bed();
        java.util.Arrays.fill(bed.getUnlocked(), true);
        return bed;
    }

    private void fill(GreenhouseData.Bed bed, List<Integer> cells, String type) {
        for (int cell : cells) bed.getPlants().put(cell, new GreenhouseData.Plant(type, 0));
    }

    @Test
    void sizesComeFromTheCatalogue() {
        assertEquals(2, catalogue.mutation("NOCTILUME").footprint());
        assertEquals(2, catalogue.mutation("PLANTBOY_ADVANCE").footprint());
        assertEquals(2, catalogue.mutation("GLASSCORN").footprint());
        assertEquals(3, catalogue.mutation("SNOOZLING").footprint());
        assertEquals(3, catalogue.mutation("GODSEED").footprint());
        assertEquals(1, catalogue.mutation("DUSTGRAIN").footprint());
    }

    @Test
    void noctilumeNeedsTwelveBlocksAroundItsTwoByTwoArea() {
        GreenhouseData.Bed bed = bed();
        int anchor = GreenhouseData.slot(4, 4);
        List<Integer> ring = Footprint.ring(anchor, 2);
        fill(bed, ring.subList(0, 6), "DUSKBLOOM");
        fill(bed, ring.subList(6, 12), "LONELILY");
        assertTrue(MutationSpread.eligible(catalogue.mutation("NOCTILUME"), bed, anchor, Map.of()));
        bed.getPlants().remove(ring.get(0));
        assertFalse(MutationSpread.eligible(catalogue.mutation("NOCTILUME"), bed, anchor, Map.of()));
    }

    @Test
    void snoozlingUsesTheWholeSixteenBlockRing() {
        GreenhouseData.Bed bed = bed();
        int anchor = GreenhouseData.slot(3, 3);
        List<Integer> ring = Footprint.ring(anchor, 3);
        fill(bed, ring.subList(0, 4), "CREAMBLOOM");
        fill(bed, ring.subList(4, 7), "DUSTGRAIN");
        fill(bed, ring.subList(7, 10), "WITHERBLOOM");
        fill(bed, ring.subList(10, 13), "DUSKBLOOM");
        fill(bed, ring.subList(13, 16), "THORNSHADE");
        assertTrue(MutationSpread.eligible(catalogue.mutation("SNOOZLING"), bed, anchor, Map.of()));
        bed.getPlants().remove(ring.get(15));
        assertFalse(MutationSpread.eligible(catalogue.mutation("SNOOZLING"), bed, anchor, Map.of()));
    }

    @Test
    void aFootprintNeedsEveryCellFree() {
        GreenhouseData.Bed bed = bed();
        int anchor = GreenhouseData.slot(3, 3);
        MutationCatalogue.Mutation snoozling = catalogue.mutation("SNOOZLING");
        assertTrue(MutationSpread.footprintFree(snoozling, bed, anchor, Map.of()));
        bed.getPlants().put(GreenhouseData.slot(4, 4), new GreenhouseData.Plant("WHEAT", 0));
        assertFalse(MutationSpread.footprintFree(snoozling, bed, anchor, Map.of()));
    }

    @Test
    void aFootprintCannotHangOverTheEdgeOrLockedSlots() {
        GreenhouseData.Bed bed = bed();
        MutationCatalogue.Mutation noctilume = catalogue.mutation("NOCTILUME");
        assertFalse(MutationSpread.footprintFree(noctilume, bed, GreenhouseData.slot(9, 9), Map.of()));
        bed.getUnlocked()[GreenhouseData.slot(5, 5)] = false;
        assertFalse(MutationSpread.footprintFree(noctilume, bed, GreenhouseData.slot(4, 4), Map.of()));
    }

    @Test
    void pendingSpawnsBlockOverlappingFootprints() {
        GreenhouseData.Bed bed = bed();
        Map<Integer, String> placed = new HashMap<>();
        placed.put(GreenhouseData.slot(3, 3), "SNOOZLING");
        assertFalse(MutationSpread.footprintFree(catalogue.mutation("DUSTGRAIN"), bed, GreenhouseData.slot(4, 4), placed));
        assertTrue(MutationSpread.footprintFree(catalogue.mutation("DUSTGRAIN"), bed, GreenhouseData.slot(6, 6), placed));
    }

    @Test
    void everyOccupiedCellResolvesToTheAnchor() {
        GreenhouseData.Bed bed = bed();
        int anchor = GreenhouseData.slot(2, 2);
        bed.getPlants().put(anchor, new GreenhouseData.Plant("SNOOZLING", 0));
        for (int cell : Footprint.cells(anchor, 3)) assertEquals(anchor, bed.anchorAt(cell));
        assertEquals(-1, bed.anchorAt(GreenhouseData.slot(5, 5)));
        assertTrue(bed.occupied(GreenhouseData.slot(4, 4)));
    }

    @Test
    void eachRingCellOfAMultiBlockNeighbourCountsAsABlock() {
        GreenhouseData.Bed bed = bed();
        bed.getPlants().put(GreenhouseData.slot(2, 3), new GreenhouseData.Plant("SNOOZLING", 0));
        List<String> around = MutationSpread.neighbourTypes(bed, GreenhouseData.slot(5, 4), 1);
        assertEquals(3, around.stream().filter("SNOOZLING"::equals).count());
    }

    @Test
    void multiBlockNeighboursShareTheirEffectsOnce() {
        GreenhouseData.Bed bed = bed();
        bed.getPlants().put(GreenhouseData.slot(2, 3), new GreenhouseData.Plant("SNOOZLING", 0));
        bed.getPlants().put(GreenhouseData.slot(5, 4), new GreenhouseData.Plant("WHEAT", 0));
        Map<Integer, List<GreenhouseEffect>> effects = GreenhouseRules.appliedEffects(bed, GreenhouseConfig.get());
        assertEquals(List.of(GreenhouseEffect.BONUS_DROPS), effects.get(GreenhouseData.slot(5, 4)));
        assertEquals(List.of(GreenhouseEffect.HARVEST_BOOST), effects.get(GreenhouseData.slot(2, 3)));
    }

    @Test
    void diagonalOnlyContactDoesNotShareEffects() {
        GreenhouseData.Bed bed = bed();
        bed.getPlants().put(GreenhouseData.slot(2, 3), new GreenhouseData.Plant("SNOOZLING", 0));
        bed.getPlants().put(GreenhouseData.slot(5, 6), new GreenhouseData.Plant("WHEAT", 0));
        assertTrue(GreenhouseRules.appliedEffects(bed, GreenhouseConfig.get()).get(GreenhouseData.slot(5, 6)).isEmpty());
    }

    @Test
    void ashwreathGrowsOnlyOnSoulSand() {
        GreenhouseData.Bed bed = bed();
        int centre = GreenhouseData.slot(5, 5);
        fill(bed, List.of(GreenhouseData.slot(4, 5), GreenhouseData.slot(6, 5)), "NETHER_WART");
        MutationCatalogue.Mutation ashwreath = catalogue.mutation("ASHWREATH");
        assertFalse(MutationSpread.eligible(ashwreath, bed, centre, Map.of()));
        bed.setSurface(centre, Surface.SOUL_SAND);
        assertTrue(MutationSpread.eligible(ashwreath, bed, centre, Map.of()));
    }

    @Test
    void veilshroomNeedsTwoMushroomsOnMycelium() {
        GreenhouseData.Bed bed = bed();
        int centre = GreenhouseData.slot(5, 5);
        fill(bed, List.of(GreenhouseData.slot(4, 5)), "MUSHROOM");
        MutationCatalogue.Mutation veilshroom = catalogue.mutation("VEILSHROOM");
        bed.setSurface(centre, Surface.MYCELIUM);
        assertFalse(MutationSpread.eligible(veilshroom, bed, centre, Map.of()));
        fill(bed, List.of(GreenhouseData.slot(6, 5)), "MUSHROOM");
        assertTrue(MutationSpread.eligible(veilshroom, bed, centre, Map.of()));
        bed.setSurface(centre, Surface.FARMLAND);
        assertFalse(MutationSpread.eligible(veilshroom, bed, centre, Map.of()));
    }

    @Test
    void lonelilyAcceptsFarmlandAndDirtButNotSand() {
        GreenhouseData.Bed bed = bed();
        bed.getPlants().put(GreenhouseData.slot(0, 0), new GreenhouseData.Plant("WHEAT", 0));
        int centre = GreenhouseData.slot(6, 6);
        MutationCatalogue.Mutation lonelily = catalogue.mutation("LONELILY");
        assertTrue(MutationSpread.eligible(lonelily, bed, centre, Map.of()));
        bed.setSurface(centre, Surface.DIRT);
        assertTrue(MutationSpread.eligible(lonelily, bed, centre, Map.of()));
        bed.setSurface(centre, Surface.SAND);
        assertFalse(MutationSpread.eligible(lonelily, bed, centre, Map.of()));
    }

    @Test
    void shadevineAcceptsSandAndFarmland() {
        GreenhouseData.Bed bed = bed();
        int centre = GreenhouseData.slot(5, 5);
        fill(bed, List.of(GreenhouseData.slot(4, 5)), "CACTUS");
        fill(bed, List.of(GreenhouseData.slot(6, 5)), "SUGAR_CANE");
        MutationCatalogue.Mutation shadevine = catalogue.mutation("SHADEVINE");
        assertTrue(MutationSpread.eligible(shadevine, bed, centre, Map.of()));
        bed.setSurface(centre, Surface.SAND);
        assertTrue(MutationSpread.eligible(shadevine, bed, centre, Map.of()));
        bed.setSurface(centre, Surface.SOUL_SAND);
        assertFalse(MutationSpread.eligible(shadevine, bed, centre, Map.of()));
    }

    @Test
    void godseedNeedsEveryPositiveEffectNearby() {
        GreenhouseData.Bed bed = bed();
        int anchor = GreenhouseData.slot(3, 3);
        List<Integer> ring = Footprint.ring(anchor, 3);
        List<String> crops = List.of("NETHER_WART", "SUGAR_CANE", "CACTUS", "POTATO", "PUMPKIN", "WILD_ROSE");
        for (int i = 0; i < crops.size(); i++) bed.getPlants().put(ring.get(i), new GreenhouseData.Plant(crops.get(i), 0));
        MutationCatalogue.Mutation godseed = catalogue.mutation("GODSEED");
        assertTrue(MutationSpread.eligible(godseed, bed, anchor, Map.of()));
        bed.getPlants().remove(ring.get(5));
        assertFalse(MutationSpread.eligible(godseed, bed, anchor, Map.of()));
    }

    @Test
    void multiBlockMutationsSpreadOverManyStages() {
        GreenhouseData.Bed bed = bed();
        int anchor = GreenhouseData.slot(4, 4);
        List<Integer> ring = Footprint.ring(anchor, 2);
        fill(bed, ring.subList(0, 6), "DUSKBLOOM");
        fill(bed, ring.subList(6, 12), "LONELILY");
        Random random = new Random(11);
        boolean grew = false;
        for (int i = 0; i < 400 && !grew; i++) {
            GreenhouseData.Bed copy = bed();
            copy.getPlants().putAll(bed.getPlants());
            grew = MutationSpread.roll(copy, 0, random).stream().anyMatch(slot -> "NOCTILUME".equals(copy.getPlants().get(slot).getType()));
        }
        assertTrue(grew);
    }
}
