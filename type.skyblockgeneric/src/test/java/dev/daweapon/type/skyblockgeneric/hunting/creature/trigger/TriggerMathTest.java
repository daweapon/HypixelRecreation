package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggerMathTest {
    private static RandomGenerator fixed(double value) {
        return new RandomGenerator() {
            @Override
            public long nextLong() {
                return 0;
            }

            @Override
            public double nextDouble() {
                return value;
            }
        };
    }

    @Test
    void lurkerScalesChance() {
        assertEquals(7.5, TriggerMath.withLurker(5, 50), 1e-9);
        assertEquals(5, TriggerMath.withLurker(5, 0), 1e-9);
    }

    @Test
    void timedDelayStaysInWindow() {
        assertEquals(8 * 60_000L, TriggerMath.delayMillis(8, 13, fixed(0)));
        assertEquals(13 * 60_000L, TriggerMath.delayMillis(8, 13, fixed(0.999999)));
    }

    @Test
    void hourChangeIgnoresFirstSample() {
        assertFalse(TriggerMath.hourChanged(-1, 5));
        assertFalse(TriggerMath.hourChanged(5, 5));
        assertTrue(TriggerMath.hourChanged(5, 6));
    }

    @Test
    void antCountStaysInRange() {
        assertEquals(2, TriggerMath.antCount(2, 4, fixed(0)));
        assertEquals(4, TriggerMath.antCount(2, 4, fixed(0.999999)));
    }
}
