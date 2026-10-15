package dev.daweapon.type.kuudra.fight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraBossStateTest {

    private static KuudraBossState surfaced(boolean cycling) {
        KuudraBossState state = new KuudraBossState(100_000, 30, 4, 6, 60);
        state.setCycling(cycling);
        state.rise();
        for (int i = 0; i < 59; i++) assertEquals(KuudraBossState.Event.NONE, state.tick());
        assertEquals(KuudraBossState.Event.SURFACED, state.tick());
        return state;
    }

    @Test
    void heRisesOverTheMoveTimeAndCanThenBeShot() {
        KuudraBossState state = new KuudraBossState(100_000, 30, 4, 6, 60);
        assertEquals(KuudraBossState.Stance.DORMANT, state.stance());
        assertEquals(1, state.depth());
        assertFalse(state.canBeShot());
        state.rise();
        for (int i = 0; i < 30; i++) state.tick();
        assertEquals(0.5, state.depth(), 1e-9);
        for (int i = 0; i < 30; i++) state.tick();
        assertEquals(KuudraBossState.Stance.SURFACED, state.stance());
        assertTrue(state.canBeShot());
    }

    @Test
    void theLoopSinksHimAfterTheSurfaceTimeAndRaisesHimAgain() {
        KuudraBossState state = surfaced(true);
        assertEquals(30, state.secondsUntilSubmerge());
        for (int i = 0; i < 600; i++) state.tick();
        assertEquals(KuudraBossState.Stance.SINKING, state.stance());
        KuudraBossState.Event last = KuudraBossState.Event.NONE;
        for (int i = 0; i < 60; i++) last = state.tick();
        assertEquals(KuudraBossState.Event.SUBMERGED, last);
        for (int i = 0; i < 120; i++) state.tick();
        assertEquals(KuudraBossState.Stance.RISING, state.stance());
    }

    @Test
    void heCannotBeShotRightBeforeHeSinks() {
        KuudraBossState state = surfaced(true);
        for (int i = 0; i < 600 - KuudraBossState.SHOT_GRACE_TICKS; i++) state.tick();
        assertFalse(state.canBeShot());
        assertFalse(state.shoot(10_000));
        assertEquals(100_000, state.health());
    }

    @Test
    void aFullVolleySendsHimUnderEarly() {
        KuudraBossState state = surfaced(true);
        for (int i = 0; i < 3; i++) assertFalse(state.shoot(6_250));
        assertEquals(KuudraBossState.Stance.SURFACED, state.stance());
        state.shoot(6_250);
        assertEquals(KuudraBossState.Stance.SINKING, state.stance());
        assertEquals(75, state.percent());
    }

    @Test
    void sixteenBasicShotsKillHim() {
        KuudraBossState state = surfaced(false);
        for (int i = 0; i < 15; i++) assertFalse(state.shoot(6_250));
        assertTrue(state.shoot(6_250));
        assertEquals(0, state.health());
        assertFalse(state.damage(1));
    }

    @Test
    void aStunHoldsTheSurfaceTimerAndEndsWithARecovery() {
        KuudraBossState state = surfaced(true);
        for (int i = 0; i < 100; i++) state.tick();
        assertEquals(25, state.secondsUntilSubmerge());
        assertTrue(state.stun(140));
        assertFalse(state.stun(140));
        assertTrue(state.isStunned());
        for (int i = 0; i < 139; i++) assertEquals(KuudraBossState.Event.NONE, state.tick());
        assertEquals(25, state.secondsUntilSubmerge());
        assertEquals(KuudraBossState.Event.RECOVERED, state.tick());
        assertFalse(state.isStunned());
        assertEquals(KuudraBossState.Stance.SURFACED, state.stance());
    }

    @Test
    void heCanOnlyBeStunnedWhileUp() {
        KuudraBossState state = new KuudraBossState(100_000, 30, 4, 6, 60);
        assertFalse(state.stun(140));
        state.rise();
        state.tick();
        assertFalse(state.stun(140));
    }

    @Test
    void shotsIntoAStunnedKuudraLandRightBeforeTheSinkAndDoNotSendHimUnder() {
        KuudraBossState state = surfaced(true);
        for (int i = 0; i < 600 - KuudraBossState.SHOT_GRACE_TICKS; i++) state.tick();
        assertTrue(state.stun(140));
        for (int i = 0; i < 6; i++) assertFalse(state.shoot(5_000));
        assertEquals(KuudraBossState.Stance.SURFACED, state.stance());
        assertEquals(70, state.percent());
    }

    @Test
    void sinkingEndsAStun() {
        KuudraBossState state = surfaced(true);
        state.stun(140);
        state.sink();
        assertFalse(state.isStunned());
    }

    @Test
    void theInfernalFloorHoldsHimAtAQuarterUntilLifted() {
        KuudraBossState state = new KuudraBossState(100_000, 30, 4, 6, 60);
        state.setFloor(25_000);
        assertFalse(state.atFloor());
        assertFalse(state.damage(90_000));
        assertEquals(25_000, state.health());
        assertTrue(state.atFloor());
        state.setFloor(0);
        assertFalse(state.atFloor());
        assertTrue(state.damage(25_000));
    }

    @Test
    void theStunTiersRetimeAndCutTheSurfaceTimer() {
        KuudraBossState state = surfaced(true);
        state.retime(60, 6, 60);
        assertEquals(60, state.secondsUntilSubmerge());
        state.submergeWithin(40);
        assertEquals(40, state.secondsUntilSubmerge());
        state.submergeWithin(50);
        assertEquals(40, state.secondsUntilSubmerge());
    }

    @Test
    void staffDamageIgnoresTheLoop() {
        KuudraBossState state = new KuudraBossState(100_000, 30, 4, 6, 60);
        assertFalse(state.damage(27_500));
        assertEquals(72.5, state.percent());
        assertTrue(state.damage(1_000_000));
    }
}
