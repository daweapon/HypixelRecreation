package dev.daweapon.type.skyblockgeneric.item.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BiomeSticksTest {
    @Test
    void radiusCyclesFromOneToFiveThenWraps() {
        assertEquals(2, BiomeSticks.nextRadius(1));
        assertEquals(5, BiomeSticks.nextRadius(4));
        assertEquals(1, BiomeSticks.nextRadius(5));
        assertEquals(1, BiomeSticks.nextRadius(0));
    }

    @Test
    void areaCoversEveryFourBlockBiomeCellItTouches() {
        assertEquals(1, BiomeSticks.cells(5, 5, 1).size());
        assertEquals(4, BiomeSticks.cells(0, 0, 1).size());
        assertEquals(9, BiomeSticks.cells(6, 6, 5).size());
    }

    @Test
    void negativeCoordinatesFloorIntoTheirOwnCell() {
        assertEquals(1, BiomeSticks.cells(-3, -3, 1).size());
        assertEquals(4, BiomeSticks.cells(-1, -1, 1).size());
    }
}
