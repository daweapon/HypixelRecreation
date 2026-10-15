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
    void readyStateIsFrozenOnceTheCountdownBegins() {
        CatacombsReadyCheck check = new CatacombsReadyCheck();
        check.toggle(first);
        check.startCountdownIfAllReady(List.of(first));
        assertTrue(check.toggle(first));
        assertTrue(check.isReady(first));
    }

    @Test
    void countdownCountsFromFourToOne() {
        assertEquals(List.of(4, 3, 2, 1), CatacombsReadyCheck.countdownSeconds());
        assertEquals("<a>Dungeon starts in 4 seconds.", CatacombsMessages.startingIn(4));
        assertEquals("<a>Dungeon starts in 1 second.", CatacombsMessages.startingIn(1));
    }
}
