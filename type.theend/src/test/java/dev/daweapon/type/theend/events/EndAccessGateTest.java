package dev.daweapon.type.theend.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndAccessGateTest {

    @Test
    void islandNeedsCombatTwelve() {
        assertFalse(ActionEndAccessGate.meetsIslandLevel(11));
        assertTrue(ActionEndAccessGate.meetsIslandLevel(12));
    }

    @Test
    void sepultureNeedsCombatTwentyFive() {
        assertFalse(ActionEndAccessGate.meetsSepultureLevel(24));
        assertTrue(ActionEndAccessGate.meetsSepultureLevel(25));
    }
}
