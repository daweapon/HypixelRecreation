package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.CatacombsRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunConfig;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsLifecycleTest {
    private static CatacombsLifecycle lifecycle() {
        CatacombsRunConfig config = new CatacombsRunConfig(
                CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL),
                Map.of(UUID.randomUUID(), DungeonClassType.ARCHER, UUID.randomUUID(), DungeonClassType.TANK), 20, 30, true);
        return new CatacombsLifecycle(config, Clock.systemUTC(), 20);
    }

    @Test
    void waitsUntilBegunThenStartsInTheStartRoom() {
        CatacombsLifecycle lifecycle = lifecycle();
        assertEquals(CatacombsLifecycle.Stage.WAITING, lifecycle.stage());
        assertNull(lifecycle.state());
        assertTrue(lifecycle.begin());
        assertFalse(lifecycle.begin());
        assertEquals(CatacombsLifecycle.Stage.ACTIVE, lifecycle.stage());
        assertEquals(DungeonRunPhase.START_ROOM, lifecycle.phase());
    }

    @Test
    void phasesOnlyMoveForward() {
        CatacombsLifecycle lifecycle = lifecycle();
        assertFalse(lifecycle.advanceTo(DungeonRunPhase.CLEARING));
        lifecycle.begin();
        assertTrue(lifecycle.advanceTo(DungeonRunPhase.CLEARING));
        assertFalse(lifecycle.advanceTo(DungeonRunPhase.CLEARING));
        assertTrue(lifecycle.advanceTo(DungeonRunPhase.BOSS));
        assertFalse(lifecycle.advanceTo(DungeonRunPhase.BLOOD_ROOM));
        assertFalse(lifecycle.advanceTo(DungeonRunPhase.COMPLETED));
        assertEquals(DungeonRunPhase.BOSS, lifecycle.phase());
    }

    @Test
    void winNeedsAnActiveRunAndCompletesTheState() {
        CatacombsLifecycle lifecycle = lifecycle();
        assertFalse(lifecycle.finish(CatacombsLifecycle.Outcome.WIN));
        lifecycle.begin();
        assertTrue(lifecycle.finish(CatacombsLifecycle.Outcome.WIN));
        assertEquals(CatacombsLifecycle.Stage.ENDING, lifecycle.stage());
        assertEquals(DungeonRunPhase.COMPLETED, lifecycle.phase());
        assertFalse(lifecycle.finish(CatacombsLifecycle.Outcome.LOSE));
    }

    @Test
    void lossFailsTheStateAndCanCloseAnUnstartedRun() {
        CatacombsLifecycle started = lifecycle();
        started.begin();
        assertTrue(started.finish(CatacombsLifecycle.Outcome.LOSE));
        assertEquals(DungeonRunPhase.FAILED, started.phase());

        CatacombsLifecycle unstarted = lifecycle();
        assertTrue(unstarted.finish(CatacombsLifecycle.Outcome.LOSE));
        assertNull(unstarted.state());
    }

    @Test
    void theSixtyMinuteCapEndsTheRunAsALoss() {
        CatacombsLifecycle lifecycle = lifecycle();
        lifecycle.begin();
        for (int second = 1; second < 3600; second++) assertEquals(CatacombsLifecycle.Tick.NOTHING, lifecycle.tick());
        assertEquals(CatacombsLifecycle.Tick.TIMED_OUT, lifecycle.tick());
        assertEquals(CatacombsLifecycle.Outcome.LOSE, lifecycle.outcome());
        assertEquals(DungeonRunPhase.FAILED, lifecycle.phase());
    }

    @Test
    void shutdownIsReportedOnceWhenTheTimerRunsOut() {
        CatacombsLifecycle lifecycle = lifecycle();
        lifecycle.begin();
        lifecycle.finish(CatacombsLifecycle.Outcome.WIN);
        for (int second = 1; second < 20; second++) assertEquals(CatacombsLifecycle.Tick.NOTHING, lifecycle.tick());
        assertEquals(CatacombsLifecycle.Tick.SHUTDOWN, lifecycle.tick());
        assertEquals(CatacombsLifecycle.Tick.NOTHING, lifecycle.tick());
    }

    @Test
    void terminatesOnce() {
        CatacombsLifecycle lifecycle = lifecycle();
        assertTrue(lifecycle.terminate());
        assertFalse(lifecycle.terminate());
        assertFalse(lifecycle.begin());
    }

    @Test
    void rejectsANonPositiveShutdownTimer() {
        assertThrows(IllegalArgumentException.class, () -> new CatacombsLifecycle(lifecycle().config(), Clock.systemUTC(), 0));
    }

    @Test
    void anEmptyRunIsAbandonedAfterItsLifetime() {
        assertTrue(CatacombsLifecycle.abandoned(0, 1_000, 61_000, 60_000));
        assertFalse(CatacombsLifecycle.abandoned(0, 1_000, 30_000, 60_000));
        assertFalse(CatacombsLifecycle.abandoned(1, 1_000, 61_000, 60_000));
        assertFalse(CatacombsLifecycle.abandoned(0, 0, 61_000, 60_000));
    }
}
