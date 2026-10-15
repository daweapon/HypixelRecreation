package dev.daweapon.type.skyblockgeneric.fishing.lava;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThunderBottlesTest {
    @Test
    void thunderKeepsItsOriginalCapacityAndIds() {
        assertEquals(50_000, ThunderBottles.CAPACITY);
        assertEquals(ThunderBottles.CAPACITY, ThunderBottles.Tier.THUNDER.capacity());
        assertEquals(ThunderBottles.EMPTY_ID, ThunderBottles.Tier.THUNDER.emptyId());
        assertEquals(ThunderBottles.FULL_ID, ThunderBottles.Tier.THUNDER.fullId());
    }

    @Test
    void stormBottleFillsToFiveHundredThousand() {
        assertEquals(500_000, ThunderBottles.Tier.STORM.capacity());
        assertEquals(ThunderBottles.Tier.STORM, ThunderBottles.tierOfEmpty("STORM_IN_A_BOTTLE_EMPTY").orElseThrow());
        assertEquals("STORM_IN_A_BOTTLE", ThunderBottles.Tier.STORM.fullId());
    }

    @Test
    void onlyEmptyBottlesCharge() {
        assertTrue(ThunderBottles.tierOfEmpty("STORM_IN_A_BOTTLE").isEmpty());
        assertTrue(ThunderBottles.tierOfEmpty("THUNDER_IN_A_BOTTLE").isEmpty());
        assertTrue(ThunderBottles.tierOfEmpty("DIAMOND").isEmpty());
    }

    @Test
    void chargeScalesWithSpeedAndStopsAtCapacity() {
        assertEquals(20, ThunderBottles.chargeAfter(0, 20, 1D, 50_000));
        assertEquals(30, ThunderBottles.chargeAfter(0, 20, 1.5D, 50_000));
        assertEquals(50_000, ThunderBottles.chargeAfter(49_990, 20, 1D, 50_000));
        assertEquals(60, ThunderBottles.chargeAfter(40, 20, 1D, 500_000));
    }
}
