package dev.daweapon.type.skyblockgeneric.fishing.torrhus;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsopodDivePathTest {
    @Test
    void divesOnceAtHalfHealth() {
        assertFalse(IsopodDivePath.shouldDive(300_000, 500_000, false));
        assertTrue(IsopodDivePath.shouldDive(250_000, 500_000, false));
        assertTrue(IsopodDivePath.shouldDive(10, 500_000, false));
        assertFalse(IsopodDivePath.shouldDive(10, 500_000, true));
    }

    @Test
    void stepsTowardsTheTargetWithoutOvershooting() {
        Pos next = IsopodDivePath.step(new Pos(0, 100, 0), new Pos(0, 0, 0), 0.6);
        assertEquals(99.4, next.y(), 1e-9);
        Pos target = new Pos(1, 100, 0);
        assertEquals(target, IsopodDivePath.step(new Pos(0.5, 100, 0), target, 0.6));
    }

    @Test
    void walksEveryWaypointAndFinishesAtTheTemple() {
        Pos position = new Pos(-643.5, 157, 184.5);
        int index = 0;
        int ticks = 0;
        while (!IsopodDivePath.finished(index) && ticks++ < 10_000) {
            position = IsopodDivePath.step(position, IsopodDivePath.WAYPOINTS.get(index), IsopodDivePath.BLOCKS_PER_TICK);
            index = IsopodDivePath.nextWaypoint(position, index);
        }
        assertTrue(IsopodDivePath.finished(index));
        assertEquals(IsopodDivePath.WAYPOINTS.getLast(), position);
        assertTrue(ticks < 600);
    }
}
