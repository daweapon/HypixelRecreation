package dev.daweapon.type.skyblockgeneric.drill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrillAnvilRoutingTest {
    @Test
    void drillsGoToTheDrillSlot() {
        assertEquals(DrillAnvilRouting.Target.DRILL, DrillAnvilRouting.route(true, "MITHRIL_DRILL_1"));
    }

    @Test
    void partsAndFuelGoToTheInputSlot() {
        DrillPart part = DrillParts.all().getFirst();
        assertEquals(DrillAnvilRouting.Target.INPUT, DrillAnvilRouting.route(false, part.itemId()));
        assertEquals(DrillAnvilRouting.Target.INPUT, DrillAnvilRouting.route(false, "BIOFUEL"));
    }

    @Test
    void unrelatedItemsAreNotRouted() {
        assertEquals(DrillAnvilRouting.Target.NONE, DrillAnvilRouting.route(false, "DIAMOND_SWORD"));
        assertEquals(DrillAnvilRouting.Target.NONE, DrillAnvilRouting.route(false, null));
        assertFalse(DrillAnvilRouting.isInput(null));
        assertTrue(DrillAnvilRouting.isInput("VOLTA"));
    }

    @Test
    void mergeRespectsTheStackLimit() {
        assertEquals(10, DrillAnvilRouting.mergeable(54, 10, 64));
        assertEquals(4, DrillAnvilRouting.mergeable(60, 10, 64));
        assertEquals(0, DrillAnvilRouting.mergeable(64, 10, 64));
    }
}
