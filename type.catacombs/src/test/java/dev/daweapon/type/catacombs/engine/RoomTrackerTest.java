package dev.daweapon.type.catacombs.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTrackerTest {
    @Test
    void aRoomIsDiscoveredOnce() {
        RoomTracker tracker = new RoomTracker();
        assertTrue(tracker.discover(3));
        assertFalse(tracker.discover(3));
        assertTrue(tracker.discover(5));
        assertEquals(2, tracker.discoveredCount());
        assertTrue(tracker.isDiscovered(3));
        assertFalse(tracker.isDiscovered(4));
    }

    @Test
    void theRoomClearsOnTheLastStarredKillExactlyOnce() {
        RoomTracker tracker = new RoomTracker();
        tracker.registerStarred(2, 3);
        assertFalse(tracker.starredKilled(2));
        assertFalse(tracker.starredKilled(2));
        assertEquals(1, tracker.starredAlive(2));
        assertTrue(tracker.starredKilled(2));
        assertTrue(tracker.isCleared(2));
        assertFalse(tracker.starredKilled(2));
    }

    @Test
    void killsInARoomWithoutStarredMobsNeverClearIt() {
        RoomTracker tracker = new RoomTracker();
        assertFalse(tracker.starredKilled(7));
        assertFalse(tracker.isCleared(7));
    }

    @Test
    void forceClearReportsWhetherItChangedAnything() {
        RoomTracker tracker = new RoomTracker();
        tracker.registerStarred(1, 2);
        assertTrue(tracker.forceClear(1));
        assertFalse(tracker.forceClear(1));
        assertEquals(0, tracker.starredAlive(1));
        assertFalse(tracker.starredKilled(1));
    }

    @Test
    void roomsWithSeveralSpawnWavesAddUp() {
        RoomTracker tracker = new RoomTracker();
        tracker.registerStarred(4, 1);
        tracker.registerStarred(4, 2);
        assertTrue(tracker.hasSpawned(4));
        assertEquals(3, tracker.starredAlive(4));
    }
}
