package net.swofty.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootprintTest {
    @Test
    void sizeParsesTheCatalogueStrings() {
        assertEquals(1, Footprint.sizeOf("1x1"));
        assertEquals(2, Footprint.sizeOf("2x2"));
        assertEquals(3, Footprint.sizeOf("3x3"));
        assertEquals(1, Footprint.sizeOf(null));
        assertEquals(1, Footprint.sizeOf("weird"));
    }

    @Test
    void cellsCoverTheWholeSquare() {
        assertEquals(List.of(GreenhouseData.slot(2, 3), GreenhouseData.slot(3, 3), GreenhouseData.slot(2, 4), GreenhouseData.slot(3, 4)),
                Footprint.cells(GreenhouseData.slot(2, 3), 2));
        assertEquals(9, Footprint.cells(GreenhouseData.slot(0, 0), 3).size());
    }

    @Test
    void footprintsPastTheEdgeAreEmpty() {
        assertTrue(Footprint.cells(GreenhouseData.slot(9, 9), 2).isEmpty());
        assertTrue(Footprint.cells(GreenhouseData.slot(8, 0), 3).isEmpty());
        assertFalse(Footprint.cells(GreenhouseData.slot(8, 8), 2).isEmpty());
    }

    @Test
    void clickedSlotIsTheCentreOfOddFootprintsAndTheCornerOfEvenOnes() {
        assertEquals(GreenhouseData.slot(4, 4), Footprint.anchorFor(GreenhouseData.slot(5, 5), 3));
        assertEquals(GreenhouseData.slot(5, 5), Footprint.anchorFor(GreenhouseData.slot(5, 5), 2));
        assertEquals(GreenhouseData.slot(5, 5), Footprint.anchorFor(GreenhouseData.slot(5, 5), 1));
        assertEquals(-1, Footprint.anchorFor(GreenhouseData.slot(0, 0), 3));
    }

    @Test
    void ringSizesMatchTheWikiBlockCounts() {
        assertEquals(8, Footprint.ring(GreenhouseData.slot(4, 4), 1).size());
        assertEquals(12, Footprint.ring(GreenhouseData.slot(4, 4), 2).size());
        assertEquals(16, Footprint.ring(GreenhouseData.slot(4, 4), 3).size());
    }

    @Test
    void ringIsClippedByTheBedEdge() {
        assertEquals(3, Footprint.ring(GreenhouseData.slot(0, 0), 1).size());
        assertEquals(2, Footprint.edge(GreenhouseData.slot(0, 0), 1).size());
        assertEquals(12, Footprint.edge(GreenhouseData.slot(4, 4), 3).size());
    }

    @Test
    void ringNeverContainsTheFootprint() {
        List<Integer> own = Footprint.cells(GreenhouseData.slot(3, 3), 3);
        for (int cell : Footprint.ring(GreenhouseData.slot(3, 3), 3)) assertFalse(own.contains(cell));
    }
}
