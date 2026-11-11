package dev.daweapon.type.hub.carnival;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZombieShootoutGameTest {
    @Test
    void timerNameMatchesTheCapturedColours() {
        assertEquals("<a>3.0s", ZombieShootoutGame.timerName(60));
        assertEquals("<a>2.0s", ZombieShootoutGame.timerName(41));
        assertEquals("<e>2.0s", ZombieShootoutGame.timerName(40));
        assertEquals("<e>1.1s", ZombieShootoutGame.timerName(22));
        assertEquals("<c>0.8s", ZombieShootoutGame.timerName(17));
        assertEquals("<c>0.1s", ZombieShootoutGame.timerName(2));
    }

    @Test
    void segmentEntersBoxAtTheRightFraction() {
        Vec min = new Vec(4, -1, -1), max = new Vec(6, 1, 1);
        assertEquals(0.4, ZombieShootoutGame.segmentBoxEntry(Vec.ZERO, new Vec(10, 0, 0), min, max), 1e-9);
        assertEquals(-1, ZombieShootoutGame.segmentBoxEntry(Vec.ZERO, new Vec(3, 0, 0), min, max));
        assertEquals(-1, ZombieShootoutGame.segmentBoxEntry(new Vec(0, 2, 0), new Vec(10, 2, 0), min, max));
    }
}
