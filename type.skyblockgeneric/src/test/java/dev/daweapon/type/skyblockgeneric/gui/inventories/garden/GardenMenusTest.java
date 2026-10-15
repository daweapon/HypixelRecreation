package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GardenMenusTest {
    @Test
    void percentIsFlooredToOneDecimalAndTrimmed() {
        assertEquals("0", GardenMenus.percent(0, 100));
        assertEquals("0", GardenMenus.percent(5, 0));
        assertEquals("50", GardenMenus.percent(50, 100));
        assertEquals("33.3", GardenMenus.percent(1, 3));
        assertEquals("100", GardenMenus.percent(500, 100));
    }

    @Test
    void barFillNeverExceedsWidth() {
        assertEquals(0, GardenMenus.filled(0, 100, 20));
        assertEquals(10, GardenMenus.filled(50, 100, 20));
        assertEquals(20, GardenMenus.filled(500, 100, 20));
        assertEquals(0, GardenMenus.filled(5, 0, 20));
    }

    @Test
    void spacesClampsNegativeCounts() {
        assertEquals("", GardenMenus.spaces(-3));
        assertEquals("   ", GardenMenus.spaces(3));
    }
}
