package dev.daweapon.type.game.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultGameCountdownTest {
    private final List<Object> events = new ArrayList<>();

    private DefaultGameCountdown countdown(boolean canStart) {
        return new DefaultGameCountdown(null, CountdownConfig.simple(20), events::add, () -> {}, () -> canStart);
    }

    @Test
    void startsWithConfiguredDuration() {
        assertEquals(20, countdown(true).getRemainingSeconds());
    }

    @Test
    void startRefusedWhenCheckFails() {
        DefaultGameCountdown countdown = countdown(false);
        assertFalse(countdown.start());
        assertFalse(countdown.isActive());
        assertTrue(events.isEmpty());
    }

    @Test
    void inactiveCountdownCannotPauseOrResume() {
        DefaultGameCountdown countdown = countdown(true);
        assertFalse(countdown.pause());
        assertFalse(countdown.resume());
        assertFalse(countdown.isPaused());
    }

    @Test
    void setRemainingSecondsClampsAtZeroAndStaysQuietWhenInactive() {
        DefaultGameCountdown countdown = countdown(true);
        countdown.setRemainingSeconds(-5);
        assertEquals(0, countdown.getRemainingSeconds());
        countdown.setRemainingSeconds(12);
        assertEquals(12, countdown.getRemainingSeconds());
        assertTrue(events.isEmpty());
    }

    @Test
    void accelerateOnlyShortensTheCountdown() {
        DefaultGameCountdown countdown = countdown(true);
        countdown.accelerate(30);
        assertEquals(20, countdown.getRemainingSeconds());
        countdown.accelerate(10);
        assertEquals(10, countdown.getRemainingSeconds());
    }
}
