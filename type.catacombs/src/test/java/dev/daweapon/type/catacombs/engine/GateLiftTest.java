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
}
