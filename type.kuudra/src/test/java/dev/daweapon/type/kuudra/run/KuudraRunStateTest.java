package dev.daweapon.type.kuudra.run;

import dev.daweapon.type.game.game.GameState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraRunStateTest {

    private static KuudraRunState basic() {
        return new KuudraRunState(false, false, 35, 100, 20);
    }

    private static KuudraRunState infernal() {
        return new KuudraRunState(true, true, 15, 100, 20);
    }

    @Test
    void waitsUntilStartedThenBeginsWithSuppliesAndWaveOne() {
        KuudraRunState state = basic();
        assertEquals(GameState.WAITING, state.stage());
        assertNull(state.phase());
        assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertFalse(state.advancePhase());

        assertTrue(state.start());
        assertEquals(GameState.IN_PROGRESS, state.stage());
        assertEquals(KuudraPhase.SUPPLIES, state.phase());
        assertEquals(1, state.wave());
        assertEquals(35, state.secondsToNextWave());
        assertFalse(state.start());
    }

    @Test
    void basicPhasesEndAfterShootingKuudra() {
        KuudraRunState state = basic();
        state.start();
        List<KuudraPhase> seen = new ArrayList<>(List.of(state.phase()));
        while (state.advancePhase()) seen.add(state.phase());
        assertEquals(List.of(KuudraPhase.SUPPLIES, KuudraPhase.BUILD, KuudraPhase.FUEL, KuudraPhase.DPS), seen);
        assertFalse(state.enterPhase(KuudraPhase.STUN));
        assertFalse(state.enterPhase(KuudraPhase.LAIR));
    }

    @Test
    void stunTiersCycleBetweenStunAndDamage() {
        KuudraRunState state = infernal();
        state.start();
        List<KuudraPhase> seen = new ArrayList<>(List.of(state.phase()));
        for (int i = 0; i < 6; i++) {
            assertTrue(state.advancePhase());
            seen.add(state.phase());
        }
        assertEquals(List.of(KuudraPhase.SUPPLIES, KuudraPhase.BUILD, KuudraPhase.FUEL, KuudraPhase.STUN,
                KuudraPhase.DPS, KuudraPhase.STUN, KuudraPhase.DPS), seen);
        assertTrue(state.enterPhase(KuudraPhase.LAIR));
        assertFalse(state.advancePhase());
        assertFalse(state.enterPhase(KuudraPhase.LAIR));
    }

    @Test
    void wavesAdvanceOnTheTierTimerAndStopAtTheCap() {
        KuudraRunState state = new KuudraRunState(false, false, 3, 3, 5);
        state.start();
        assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertEquals(KuudraRunState.Tick.NEW_WAVE, state.tick());
        assertEquals(2, state.wave());
        assertEquals(3, state.secondsToNextWave());
        state.tick();
        state.tick();
        assertEquals(KuudraRunState.Tick.NEW_WAVE, state.tick());
        assertEquals(3, state.wave());
        for (int i = 0; i < 10; i++) assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertEquals(3, state.wave());
        assertEquals(0, state.secondsToNextWave());
        assertEquals(16, state.elapsedSeconds());
    }

    @Test
    void winningNeedsAStartedFightButLosingDoesNot() {
        KuudraRunState waiting = basic();
        assertFalse(waiting.finish(KuudraRunState.Outcome.WIN));
        assertTrue(waiting.finish(KuudraRunState.Outcome.LOSE));
        assertEquals(GameState.ENDING, waiting.stage());

        KuudraRunState fighting = basic();
        fighting.start();
        assertTrue(fighting.finish(KuudraRunState.Outcome.WIN));
        assertEquals(KuudraRunState.Outcome.WIN, fighting.outcome());
        assertFalse(fighting.finish(KuudraRunState.Outcome.LOSE));
        assertFalse(fighting.advancePhase());
    }

    @Test
    void shutdownIsReportedOnceWhenTheTimerRunsOut() {
        KuudraRunState state = new KuudraRunState(false, false, 35, 100, 3);
        state.start();
        state.finish(KuudraRunState.Outcome.LOSE);
        assertEquals(3, state.shutdownSecondsLeft());
        assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertEquals(KuudraRunState.Tick.SHUTDOWN, state.tick());
        assertEquals(KuudraRunState.Tick.NOTHING, state.tick());
        assertTrue(state.terminate());
        assertFalse(state.terminate());
        assertEquals(GameState.TERMINATED, state.stage());
    }

    @Test
    void emptyRunsAreAbandonedOnlyAfterTheLifetime() {
        assertFalse(KuudraRunState.abandoned(1, 1_000, 100_000, 60_000));
        assertFalse(KuudraRunState.abandoned(0, 0, 100_000, 60_000));
        assertFalse(KuudraRunState.abandoned(0, 50_000, 100_000, 60_000));
        assertTrue(KuudraRunState.abandoned(0, 40_000, 100_000, 60_000));
    }

    @Test
    void rejectsImpossibleTimings() {
        assertThrows(IllegalArgumentException.class, () -> new KuudraRunState(false, false, 0, 100, 20));
        assertThrows(IllegalArgumentException.class, () -> new KuudraRunState(false, false, 35, 100, 0));
    }
}
