package net.swofty.type.skyblockgeneric.glacite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColdMathTest {
    @Test
    void dropsOnePointEveryFiveSecondsWithNoModifiers() {
        assertEquals(5, ColdMath.secondsPerPoint(0, 0), 1e-9);
    }

    @Test
    void waterVulnerabilityQuartersTheInterval() {
        assertEquals(1.25, ColdMath.secondsPerPoint(0, ColdMath.WATER_VULNERABILITY), 1e-9);
    }

    @Test
    void resistanceSlowsAccumulation() {
        assertEquals(10, ColdMath.secondsPerPoint(100, 0), 1e-9);
    }

    @Test
    void eachPointCostsHalfAPercentOfMiningSpeed() {
        assertEquals(0.9, ColdMath.miningSpeedMultiplier(-20, 0), 1e-9);
    }

    @Test
    void resistanceAbsorbsColdBeforeAnyPenalty() {
        assertEquals(1.0, ColdMath.miningSpeedMultiplier(-10, 10), 1e-9);
        assertEquals(0.95, ColdMath.miningSpeedMultiplier(-20, 10), 1e-9);
    }

    @Test
    void everyTwentyFiveColdCapsSpeedByAnotherQuarterRegardlessOfResistance() {
        assertEquals(0.75, ColdMath.miningSpeedMultiplier(-25, 1000), 1e-9);
        assertEquals(0.5, ColdMath.miningSpeedMultiplier(-50, 1000), 1e-9);
    }

    @Test
    void coldIsClampedBetweenMinusHundredAndZero() {
        assertEquals(-100, ColdMath.clamp(-250));
        assertEquals(0, ColdMath.clamp(30));
        assertTrue(ColdMath.freezesToDeath(-100));
        assertFalse(ColdMath.freezesToDeath(-99.5));
    }

    @Test
    void messagesFireOnlyWhenAThresholdIsCrossedDownwards() {
        assertEquals(1, ColdMath.crossed(-24, -25).size());
        assertTrue(ColdMath.crossed(-26, -27).isEmpty());
        assertTrue(ColdMath.crossed(-30, -20).isEmpty());
        assertEquals(2, ColdMath.crossed(-20, -60).size());
    }
}
