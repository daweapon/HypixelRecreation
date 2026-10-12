package net.swofty.type.skyblockgeneric.consumable;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaltTest {
    @Test
    void activeOnlyBeforeExpiration() {
        Map<String, Long> expirations = Map.of("OCEANDY", 5000L);
        assertTrue(TimedConsumables.isActive(expirations, "OCEANDY", 4999));
        assertFalse(TimedConsumables.isActive(expirations, "OCEANDY", 5000));
        assertFalse(TimedConsumables.isActive(expirations, "CANDYCOMB", 0));
    }

    @Test
    void activeSaltsStackWhenTheyApply() {
        Map<String, Long> expirations = Map.of("OCEANDY", 5000L, "CANDYCOMB", 5000L, "BRINE_TONIC", 100L);
        assertEquals(2.5, Salt.charmChanceBonus(expirations, 1000, salt -> true), 1e-9);
        assertEquals(1.25, Salt.charmChanceBonus(expirations, 1000, salt -> salt == Salt.OCEANDY), 1e-9);
        assertEquals(0, Salt.charmChanceBonus(Map.of(), 1000, salt -> true));
    }
}
