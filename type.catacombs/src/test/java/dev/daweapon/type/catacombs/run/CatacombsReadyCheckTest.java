package dev.daweapon.type.catacombs.run;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsReadyCheckTest {
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();

    @Test
    void togglingFlipsTheReadyState() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        assertTrue(check.toggle(first));
        assertTrue(check.isReady(first));
        assertFalse(check.toggle(first));
        assertFalse(check.isReady(first));
    }

    @Test
    void startsOnlyWhenEveryonePresentIsReady() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        check.toggle(first);
        assertFalse(check.startCountdownIfAllReady(List.of(first, second)));
        check.toggle(second);
        assertTrue(check.startCountdownIfAllReady(List.of(first, second)));
        assertFalse(check.startCountdownIfAllReady(List.of(first, second)));
        assertTrue(check.countingDown());
    }

    @Test
    void nobodyPresentNeverStarts() {
        assertFalse(new CatacombsReadyCheck().startCountdownIfAllReady(List.of()));
        assertFalse(new CatacombsReadyCheck().tickSecond(List.of()));
    }

    @Test
    void theTimeoutStartsTheCountdownAfterThirtySeconds() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        for (int second = 1; second < CatacombsReadyCheck.TIMEOUT_SECONDS; second++) assertFalse(check.tickSecond(List.of(first)));
        assertEquals(1, check.secondsLeft());
        assertTrue(check.tickSecond(List.of(first)));
        assertFalse(check.tickSecond(List.of(first)));
    }

    @Test
    void unreadyingCancelsAnAllReadyCountdown() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        check.toggle(first);
        check.startCountdownIfAllReady(List.of(first));
        int id = check.countdown();
        assertTrue(check.stillCounting(id));
        assertTrue(check.unready(first));
        assertFalse(check.isReady(first));
        assertFalse(check.countingDown());
        assertFalse(check.stillCounting(id));
        check.markReady(first);
        assertTrue(check.startCountdownIfAllReady(List.of(first)));
        assertFalse(check.stillCounting(id));
        assertTrue(check.stillCounting(check.countdown()));
    }

    @Test
    void theTimeoutCountdownCannotBeCancelled() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        check.toggle(first);
        for (int tick = 1; tick <= CatacombsReadyCheck.TIMEOUT_SECONDS; tick++) check.tickSecond(List.of(first, second));
        assertTrue(check.countingDown());
        assertFalse(check.unready(first));
        assertTrue(check.isReady(first));
    }

    @Test
    void markReadyIsIdempotentAndStopsAtTheCountdown() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        assertTrue(check.markReady(first));
        assertFalse(check.markReady(first));
        assertTrue(check.isReady(first));
        assertTrue(check.startCountdownIfAllReady(List.of(first)));
        assertFalse(check.markReady(second));
    }

    @Test
    void countdownCountsFromFourToOne() {
        assertEquals(List.of(4, 3, 2, 1), CatacombsReadyCheck.countdownSeconds());
        assertEquals("<a>Starting in 4 seconds.", CatacombsMessages.startingIn(4));
        assertEquals("<a>Starting in 1 second.", CatacombsMessages.startingIn(1));
    }
}
