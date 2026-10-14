package net.swofty.type.game.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountdownConfigTest {
    @Test
    void defaultAnnouncesAtConfiguredMarks() {
        CountdownConfig config = CountdownConfig.DEFAULT;
        assertEquals(30, config.durationSeconds());
        assertTrue(config.shouldAnnounce(30));
        assertTrue(config.shouldAnnounce(1));
        assertFalse(config.shouldAnnounce(25));
    }

    @Test
    void simpleUsesSameDurationWhenAccelerated() {
        CountdownConfig config = CountdownConfig.simple(15);
        assertEquals(15, config.durationSeconds());
        assertEquals(15, config.acceleratedDurationSeconds());
        assertEquals(-1, config.accelerationPlayerThreshold());
        assertFalse(config.shouldAnnounce(30));
        assertTrue(config.shouldAnnounce(10));
    }

    @Test
    void withAccelerationKeepsThresholds() {
        CountdownConfig config = CountdownConfig.withAcceleration(30, 10, 8);
        assertEquals(10, config.acceleratedDurationSeconds());
        assertEquals(8, config.accelerationPlayerThreshold());
    }

    @Test
    void negativeDurationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CountdownConfig(-1, new int[0], 5, -1));
        assertThrows(IllegalArgumentException.class, () -> new CountdownConfig(5, new int[0], -1, -1));
    }

    @Test
    void announcementTimesAreDefensivelyCopied() {
        int[] source = {5, 3};
        CountdownConfig config = new CountdownConfig(10, source, 10, -1);
        source[0] = 99;
        config.announcementTimes()[1] = 99;
        assertTrue(config.shouldAnnounce(5));
        assertTrue(config.shouldAnnounce(3));
        assertFalse(config.shouldAnnounce(99));
    }

    @Test
    void gameStateHelpers() {
        assertTrue(GameState.WAITING.isWaiting());
        assertTrue(GameState.COUNTDOWN.isWaiting());
        assertTrue(GameState.IN_PROGRESS.isInProgress());
        assertTrue(GameState.ENDING.isEnding());
        assertTrue(GameState.TERMINATED.isEnding());
        assertFalse(GameState.IN_PROGRESS.isWaiting());
    }
}
