package dev.daweapon.type.generic.entity.npc.walk;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalkRouteTest {
    private final Pos destination = new Pos(10, 64, 10, 180, 0);
    private final WalkRoute route = new WalkRoute(List.of(new Vec(0, 64, 0), new Vec(0, 64, 10), new Vec(10, 64, 10)), destination);

    @Test
    void measuresEverySegment() {
        assertEquals(20, route.length(), 1.0E-9);
        assertEquals(100, WalkRoute.ticksToWalk(route.length(), NPCRelocation.WALK_BLOCKS_PER_TICK));
    }

    @Test
    void interpolatesAlongTheRouteFacingTheWayItWalks() {
        Pos start = route.at(0, 45);
        assertEquals(new Vec(0, 64, 0), start.asVec());
        assertEquals(0, start.yaw(), 1.0E-4, "walking towards +Z faces south (yaw 0)");

        Pos quarter = route.at(5, 0);
        assertEquals(new Vec(0, 64, 5), quarter.asVec());

        Pos threeQuarters = route.at(15, 0);
        assertEquals(new Vec(5, 64, 10), threeQuarters.asVec());
        assertEquals(-90, threeQuarters.yaw(), 1.0E-4, "walking towards +X faces east (yaw -90)");
    }

    @Test
    void clampsPastTheEndToTheLastPoint() {
        assertEquals(new Vec(10, 64, 10), route.at(500, 0).asVec());
    }

    @Test
    void aSinglePointRouteKeepsTheCurrentFacing() {
        WalkRoute stay = new WalkRoute(List.of(new Vec(3, 64, 3)), destination);
        assertEquals(0, stay.length());
        assertEquals(30, stay.at(0, 30).yaw(), 1.0E-4);
    }

    @Test
    void choosesWalkDepartOrTeleport() {
        assertEquals(NPCRelocation.TELEPORT, NPCRelocation.decide(false, 5, 20));
        assertEquals(NPCRelocation.TELEPORT, NPCRelocation.decide(true, 60, 20), "nobody is watching");
        assertEquals(NPCRelocation.TELEPORT, NPCRelocation.decide(true, 5, 0.1), "already there");
        assertEquals(NPCRelocation.WALK, NPCRelocation.decide(true, 5, 41));
        assertEquals(NPCRelocation.WALK, NPCRelocation.decide(true, 47, 120));
        assertEquals(NPCRelocation.DEPART, NPCRelocation.decide(true, 5, 370), "far destinations are walked off towards");
    }

    @Test
    void vanishesOnlyOutOfSightOrAtTheEndOfAPartialRoute() {
        assertFalse(NPCRelocation.vanishes(10, true, false));
        assertFalse(NPCRelocation.vanishes(10, true, true));
        assertFalse(NPCRelocation.vanishes(10, false, false));
        assertTrue(NPCRelocation.vanishes(10, false, true));
        assertTrue(NPCRelocation.vanishes(49, true, false));
        assertTrue(NPCRelocation.DEPART.blocksPerTick() > NPCRelocation.WALK.blocksPerTick());
    }
}
