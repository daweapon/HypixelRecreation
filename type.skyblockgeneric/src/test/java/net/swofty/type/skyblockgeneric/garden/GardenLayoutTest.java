package net.swofty.type.skyblockgeneric.garden;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenLayoutTest {
    @Test
    void mapsPositionsToPlots() {
        assertEquals(GardenLayout.BARN, GardenLayout.plotAt(-5.5, 17.5));
        assertEquals(GardenLayout.BARN, GardenLayout.plotAt(-48, -48));
        assertEquals(GardenLayout.BARN, GardenLayout.plotAt(47.9, 47.9));
        assertEquals(2, GardenLayout.plotAt(-48.3, 10));
        assertEquals(3, GardenLayout.plotAt(48, 0));
        assertEquals(1, GardenLayout.plotAt(0, -48.5));
        assertEquals(4, GardenLayout.plotAt(0, 48));
        assertEquals(6, GardenLayout.plotAt(60, -100));
        assertEquals(24, GardenLayout.plotAt(239.9, 239.9));
        assertEquals(21, GardenLayout.plotAt(-240, -240));
    }

    @Test
    void outsideTheGridIsNoPlot() {
        assertEquals(GardenLayout.NONE, GardenLayout.plotAt(-240.5, 0));
        assertEquals(GardenLayout.NONE, GardenLayout.plotAt(0, 240));
    }

    @Test
    void signsSitOnEveryBorderAndNameTheOuterPlot() {
        List<GardenLayout.PlotSign> signs = GardenLayout.signs();
        assertEquals(40, signs.size());
        assertTrue(signs.contains(new GardenLayout.PlotSign(2, -48, 0)));
        assertTrue(signs.contains(new GardenLayout.PlotSign(3, 48, 0)));
        assertTrue(signs.contains(new GardenLayout.PlotSign(6, 48, -96)));
        assertTrue(signs.contains(new GardenLayout.PlotSign(1, 0, -48)));
        assertTrue(signs.contains(new GardenLayout.PlotSign(4, 0, 48)));
    }
}
