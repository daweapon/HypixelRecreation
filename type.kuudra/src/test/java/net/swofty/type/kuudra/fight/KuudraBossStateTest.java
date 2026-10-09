package net.swofty.type.kuudra.fight;

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
    void staffDamageIgnoresTheLoop() {
        KuudraBossState state = new KuudraBossState(100_000, 30, 4, 6, 60);
        assertFalse(state.damage(27_500));
        assertEquals(72.5, state.percent());
        assertTrue(state.damage(1_000_000));
    }
}
