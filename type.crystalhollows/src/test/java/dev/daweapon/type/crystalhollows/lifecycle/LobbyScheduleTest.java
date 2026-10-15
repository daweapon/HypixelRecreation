package dev.daweapon.type.crystalhollows.lifecycle;

import org.junit.jupiter.api.Test;

import static dev.daweapon.type.crystalhollows.lifecycle.LobbySchedule.MILLIS_PER_DAY;
import static dev.daweapon.type.crystalhollows.lifecycle.LobbySchedule.Phase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyScheduleTest {
    @Test
    void aDayIsTwentyRealMinutes() {
        assertEquals(0, LobbySchedule.dayAt(MILLIS_PER_DAY - 1));
        assertEquals(1, LobbySchedule.dayAt(MILLIS_PER_DAY));
        assertEquals(0, LobbySchedule.dayAt(-5));
    }

    @Test
    void lobbyAcceptsNewPlayersUntilDayNineteen() {
        assertTrue(LobbySchedule.acceptsNewPlayers(19 * MILLIS_PER_DAY - 1));
        assertFalse(LobbySchedule.acceptsNewPlayers(19 * MILLIS_PER_DAY));
    }

    @Test
    void openLobbyNeverClosesEarly() {
        assertEquals(Phase.OPEN, LobbySchedule.phaseAt(5 * MILLIS_PER_DAY, 0));
    }

    @Test
    void lockedLobbyWithEnoughPlayersStaysLockedUntilDayThirtyFive() {
        assertEquals(Phase.LOCKED, LobbySchedule.phaseAt(19 * MILLIS_PER_DAY, 4));
        assertEquals(Phase.LOCKED, LobbySchedule.phaseAt(35 * MILLIS_PER_DAY - 1, 10));
        assertEquals(Phase.CLOSED, LobbySchedule.phaseAt(35 * MILLIS_PER_DAY, 10));
    }

    @Test
    void lockedLobbyWithFewerThanFourPlayersShutsDown() {
        assertEquals(Phase.CLOSED, LobbySchedule.phaseAt(20 * MILLIS_PER_DAY, 3));
    }

    @Test
    void shutdownCountdownNeverGoesNegative() {
        assertEquals(5 * MILLIS_PER_DAY, LobbySchedule.millisUntilShutdown(30 * MILLIS_PER_DAY));
        assertEquals(0, LobbySchedule.millisUntilShutdown(40 * MILLIS_PER_DAY));
    }
}
