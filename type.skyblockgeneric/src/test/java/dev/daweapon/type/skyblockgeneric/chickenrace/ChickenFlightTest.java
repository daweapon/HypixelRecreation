package dev.daweapon.type.skyblockgeneric.chickenrace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChickenFlightTest {
    private static final ChickenFlight.Physics PHYSICS = new ChickenFlight.Physics(8, 0.9, 0.06, 0.75, 20);

    @Test
    void oneFlapClimbsAboutSixBlocks() {
        ChickenFlight flight = new ChickenFlight(PHYSICS);
        double height = 0;
        double peak = 0;
        height += flight.tick(true);
        for (int tick = 0; tick < 40; tick++) {
            height += flight.tick(false);
            peak = Math.max(peak, height);
        }
        assertTrue(peak > 5.5 && peak < 7.5, "peak " + peak);
    }

    @Test
    void holdingJumpClimbsAboutSixBlocksPerSecond() {
        ChickenFlight flight = new ChickenFlight(PHYSICS);
        double height = 0;
        for (int tick = 0; tick < 200; tick++) height += flight.tick(true);
        double perSecond = height / 10;
        assertTrue(perSecond > 4.5 && perSecond < 7, "climb " + perSecond);
    }

    @Test
    void freeFallTopsOutAtFifteenBlocksPerSecond() {
        ChickenFlight flight = new ChickenFlight(PHYSICS);
        double drop = 0;
        for (int tick = 0; tick < 100; tick++) drop = flight.tick(false);
        assertEquals(-15, drop * 20, 1e-9);
    }

    @Test
    void flapsWaitForTheCooldown() {
        ChickenFlight flight = new ChickenFlight(PHYSICS);
        flight.tick(true);
        for (int tick = 1; tick < PHYSICS.flapCooldownTicks(); tick++) flight.tick(false);
        assertFalse(flight.flapReady());
        flight.tick(false);
        assertTrue(flight.flapReady());
    }

    @Test
    void horizontalSpeedIsEightBlocksPerSecond() {
        assertEquals(0.4, PHYSICS.blocksPerTick(), 1e-9);
    }
}
