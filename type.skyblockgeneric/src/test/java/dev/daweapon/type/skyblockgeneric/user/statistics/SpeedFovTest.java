package dev.daweapon.type.skyblockgeneric.user.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeedFovTest {
    @Test
    void godPotionSpeedEightIsCountedSoTheFovStaysPut() {
        assertEquals(0.4 * 2.6, SpeedFov.clientSpeed(0.4, 8, 0), 1e-9);
    }

    @Test
    void noEffectsLeavesTheBaseSpeed() {
        assertEquals(0.1, SpeedFov.clientSpeed(0.1, 0, 0), 1e-9);
    }

    @Test
    void slownessCannotGoBelowZero() {
        assertEquals(0, SpeedFov.clientSpeed(0.1, 0, 10), 1e-9);
    }
}
