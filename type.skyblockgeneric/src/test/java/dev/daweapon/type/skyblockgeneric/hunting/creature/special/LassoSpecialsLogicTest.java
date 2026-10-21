package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LassoSpecialsLogicTest {
    @Test
    void strikeCycleRunsOrbitDiveRecover() {
        StrikeCycle cycle = new StrikeCycle(3, 5, 2);
        for (int i = 0; i < 3; i++) cycle.tick(false);
        assertEquals(StrikeCycle.Phase.DIVE, cycle.phase());
        cycle.tick(true);
        assertEquals(StrikeCycle.Phase.RECOVER, cycle.phase());
        cycle.tick(false);
        cycle.tick(false);
        assertEquals(StrikeCycle.Phase.ORBIT, cycle.phase());
    }

    @Test
    void diveGivesUpAtItsLimit() {
        StrikeCycle cycle = new StrikeCycle(1, 3, 1);
        cycle.tick(false);
        for (int i = 0; i < 3; i++) cycle.tick(false);
        assertEquals(StrikeCycle.Phase.RECOVER, cycle.phase());
    }

    @Test
    void stepTowardStopsAtTarget() {
        Pos from = new Pos(0, 0, 0);
        assertEquals(1, CreatureMotion.stepToward(from, new Pos(10, 0, 0), 1).x(), 1e-9);
        assertEquals(10, CreatureMotion.stepToward(from, new Pos(10, 0, 0), 50).x(), 1e-9);
    }

    @Test
    void lookingAtDetectsFacing() {
        Pos viewer = new Pos(0, 0, 0, 0, 0);
        assertTrue(CreatureMotion.lookingAt(viewer, 1.6, new Pos(0, 1.6, 5), LookAwayTeleportAttack.WATCHED_DOT));
        assertFalse(CreatureMotion.lookingAt(viewer, 1.6, new Pos(0, 1.6, -5), LookAwayTeleportAttack.WATCHED_DOT));
    }

    @Test
    void beeheemothLeavesAfterFiveMinutes() {
        assertFalse(TimedFlyAway5m.expired(5 * 60 * 20 - 1));
        assertTrue(TimedFlyAway5m.expired(5 * 60 * 20));
    }

    @Test
    void groundhogTossAlternatesSides() {
        Vec towardMob = new Vec(1, 0, 0);
        assertEquals(-LassoThrowsPlayer.toss(towardMob, 1).z(), LassoThrowsPlayer.toss(towardMob, 0).z(), 1e-9);
        assertTrue(LassoThrowsPlayer.toss(towardMob, 0).y() > 0);
    }
}
