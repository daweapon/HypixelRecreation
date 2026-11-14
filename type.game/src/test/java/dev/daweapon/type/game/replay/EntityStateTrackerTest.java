package dev.daweapon.type.game.replay;

import dev.daweapon.type.game.replay.entity.EntityStateTracker;
import dev.daweapon.type.game.replay.entity.EntityStateTracker.StateType;
import dev.daweapon.type.game.replay.recordable.Recordable;
import dev.daweapon.type.game.replay.recordable.RecordablePlayerSneak;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityStateTrackerTest {
    private final EntityStateTracker tracker = new EntityStateTracker();

    @Test
    void stateAtTickIsMostRecentAtOrBefore() {
        Recordable early = new RecordablePlayerSneak(1, true);
        Recordable late = new RecordablePlayerSneak(1, false);
        tracker.recordState(1, StateType.SNEAKING, 10, early);
        tracker.recordState(1, StateType.SNEAKING, 50, late);

        assertNull(tracker.getStateAt(1, StateType.SNEAKING, 9));
        assertSame(early, tracker.getStateAt(1, StateType.SNEAKING, 10));
        assertSame(early, tracker.getStateAt(1, StateType.SNEAKING, 49));
        assertSame(late, tracker.getStateAt(1, StateType.SNEAKING, 500));
    }

    @Test
    void defaultStateFillsGapsBeforeFirstRecord() {
        Recordable fallback = new RecordablePlayerSneak(1, false);
        tracker.setDefaultState(StateType.SNEAKING, fallback);
        tracker.recordState(1, StateType.SNEAKING, 10, new RecordablePlayerSneak(1, true));

        assertSame(fallback, tracker.getStateAt(1, StateType.SNEAKING, 3));
        assertSame(fallback, tracker.getStateAt(99, StateType.SNEAKING, 3));
    }

    @Test
    void statesBetweenIsInclusiveOnBothEnds() {
        for (int tick : new int[]{5, 10, 15, 20}) {
            tracker.recordState(1, StateType.SPRINTING, tick, new RecordablePlayerSneak(1, true));
        }
        assertEquals(2, tracker.getStatesBetween(1, StateType.SPRINTING, 10, 15).size());
        assertEquals(List.of(), tracker.getStatesBetween(2, StateType.SPRINTING, 0, 100));
    }

    @Test
    void trackMapsEntityStateRecordablesAtTickZero() {
        Recordable sneak = new RecordablePlayerSneak(4, true);
        tracker.track(sneak);
        assertSame(sneak, tracker.getStateAt(4, StateType.SNEAKING, 0));
        assertTrue(tracker.getTrackedEntities().contains(4));
    }

    @Test
    void allStatesAtCollectsOnlyPresentTypes() {
        tracker.recordState(1, StateType.SNEAKING, 1, new RecordablePlayerSneak(1, true));
        Map<StateType, Recordable> all = tracker.getAllStatesAt(1, 5);
        assertEquals(1, all.size());
        assertTrue(all.containsKey(StateType.SNEAKING));
    }

    @Test
    void removeEntityDropsStateSkinAndHealth() {
        tracker.recordState(1, StateType.SNEAKING, 1, new RecordablePlayerSneak(1, true));
        tracker.trackSkin(1, "v", "s");
        tracker.trackHealth(1, 10f, 20f);
        tracker.removeEntity(1);
        assertNull(tracker.getStateAt(1, StateType.SNEAKING, 5));
        assertNull(tracker.getSkin(1));
        assertNull(tracker.getHealth(1));
        assertTrue(tracker.getTrackedEntities().isEmpty());
    }
}
