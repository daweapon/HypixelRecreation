package dev.daweapon.type.generic.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TpsMeterSmoothingTest {

    @Test
    void theFirstTickSeedsTheAverage() {
        assertEquals(30, TpsMeter.smooth(0, 30));
    }

    @Test
    void oneSlowTickNudgesTheAverageWithoutTakingItOver() {
        double average = TpsMeter.smooth(10, 500);
        assertTrue(average > 10 && average < 50, "average was " + average);
    }

    @Test
    void aSustainedSlowdownConvergesOnTheNewTickTime() {
        double average = 10;
        for (int i = 0; i < 200; i++) average = TpsMeter.smooth(average, 80);
        assertEquals(80, average, 0.5);
    }
}
