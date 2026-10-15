package dev.daweapon.type.skyblockgeneric.glacite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortableCampfireTest {
    @Test
    void placementCooldownIsFiveMinutes() {
        assertEquals(300_000, PortableCampfire.cooldownRemaining(1_000, 1_000));
        assertEquals(100_000, PortableCampfire.cooldownRemaining(0, 200_000));
        assertEquals(0, PortableCampfire.cooldownRemaining(0, 300_000));
    }

    @Test
    void campfireBurnsOutAfterFiveMinutesAndRemovesFiftyCold() {
        assertFalse(PortableCampfire.expired(0, 299_999));
        assertTrue(PortableCampfire.expired(0, 300_000));
        assertEquals(50, PortableCampfire.COLD_REMOVED, 1e-9);
    }
}
