package dev.daweapon.type.catacombs.run;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostRosterTest {
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();

    @Test
    void aGhostRevivesByItselfAfterFiftySecondsWhenAutoReviveIsOn() {
        GhostRoster roster = new GhostRoster(true, 50);
        assertTrue(roster.die(first));
        assertFalse(roster.die(first));
        for (int second = 1; second < 50; second++) assertEquals(List.of(), roster.tickSecond());
        assertEquals(List.of(first), roster.tickSecond());
        assertFalse(roster.isGhost(first));
    }

    @Test
    void neverRevivesByItselfWhenAutoReviveIsOff() {
        GhostRoster roster = new GhostRoster(false, 50);
        roster.die(first);
        for (int second = 0; second < 200; second++) assertEquals(List.of(), roster.tickSecond());
        assertTrue(roster.isGhost(first));
        assertTrue(roster.revive(first));
        assertFalse(roster.revive(first));
    }

    @Test
    void theRunIsWipedOnlyWhenEveryonePresentIsAGhost() {
        GhostRoster roster = new GhostRoster(true, 50);
        roster.die(first);
        assertFalse(roster.wiped(List.of(first, second)));
        roster.die(second);
        assertTrue(roster.wiped(List.of(first, second)));
        assertFalse(roster.wiped(List.of()));
    }

    @Test
    void reportsTheSecondsLeftToRevive() {
        GhostRoster roster = new GhostRoster(true, 50);
        roster.die(first);
        roster.tickSecond();
        assertEquals(49, roster.secondsToRevive(first));
        assertEquals(1, roster.ghostCount());
    }
}
