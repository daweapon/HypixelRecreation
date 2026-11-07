package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ForwardTeleportTest {
    private static final ForwardTeleport.Passable OPEN_FIELD = (x, y, z) -> y >= 64;

    @Test
    void landsCentredInABlockTenAhead() {
        Pos landed = ForwardTeleport.destination(new Pos(0.3, 64, 0.8, 0, 0), 10, OPEN_FIELD);
        assertEquals(new Pos(0.5, 64, 10.5), landed.withView(0, 0));
    }

    @Test
    void keepsTheViewItWasCastWith() {
        Pos landed = ForwardTeleport.destination(new Pos(0.5, 64, 0.5, 30, -10), 10, OPEN_FIELD);
        assertEquals(30, landed.yaw());
        assertEquals(-10, landed.pitch());
    }

    @Test
    void stopsInTheLastOpenBlockBeforeAWall() {
        ForwardTeleport.Passable wall = (x, y, z) -> y >= 64 && z != 5;
        assertEquals(4.5, ForwardTeleport.destination(new Pos(0.5, 64, 0.5, 0, 0), 10, wall).z());
    }

    @Test
    void aWallRightAheadMeansNoTeleport() {
        ForwardTeleport.Passable wall = (x, y, z) -> y >= 64 && z < 1;
        assertNull(ForwardTeleport.destination(new Pos(0.5, 64, 0.5, 0, 0), 10, wall));
    }

    @Test
    void standingOnASlabStillTeleportsAtTheNextWholeBlock() {
        ForwardTeleport.Passable slabFloor = (x, y, z) -> y >= 65;
        Pos landed = ForwardTeleport.destination(new Pos(0.5, 64.5, 0.5, 0, 0), 10, slabFloor);
        assertEquals(65, landed.y());
    }
}
