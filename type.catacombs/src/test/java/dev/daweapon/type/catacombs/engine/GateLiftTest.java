package dev.daweapon.type.catacombs.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The captured gates hold still for a moment, then sink into the floor faster and faster. */
class GateLiftTest {
    @Test
    void theGateSinksFasterAndFasterAfterAShortPause() {
        assertEquals(0, GateLift.sinkAfter(3));
        double previousStep = 0;
        for (int tick = 4; tick <= GateLift.DURATION_TICKS; tick++) {
            double step = GateLift.sinkAfter(tick) - GateLift.sinkAfter(tick - 1);
            assertTrue(step > previousStep, "tick " + tick);
            previousStep = step;
        }
    }

    @Test
    void itMatchesTheCapturedDepthsRoughly() {
        assertEquals(1.35, GateLift.sinkAfter(13), 0.3);
        assertEquals(1.87, GateLift.sinkAfter(16), 0.3);
        assertTrue(GateLift.sinkAfter(GateLift.DURATION_TICKS) > 3, "the whole 4-tall gate is below the floor before it vanishes");
        assertTrue(GateLift.RIDER_DROP > 0.6 && GateLift.RIDER_DROP < 0.7);
    }

    @Test
    void bonzosExitGatesSinkSteadilyAndFullyBeforeTheyVanish() {
        GateLift.Motion gate = GateLift.Motion.BOSS_GATE;
        assertEquals(52, gate.openTick(), "barriers turned to air 2.6 s after they were placed");
        assertEquals(81, gate.durationTicks(), "riders removed 4.1 s after they spawned");
        assertEquals(0.29, gate.sinkAfter(9), 0.2, "first captured position sync");
        for (int tick = 20; tick <= gate.durationTicks(); tick++) {
            assertEquals(0.104, gate.sinkAfter(tick) - gate.sinkAfter(tick - 1), 1e-9, "steady 0.31 blocks every 3 ticks");
        }
        assertTrue(gate.sinkAfter(gate.openTick()) > 4.5, "nearly all of the 5-tall gate is under the floor before the barriers go");
        assertTrue(gate.sinkAfter(gate.durationTicks()) > 7);
    }
}
