package dev.daweapon.type.skyblockgeneric.foraging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SweepDetailsTest {
    @Test
    void wrongOrderShowsTheHalvedSweep() {
        String[] lines = SweepDetails.lines("FIG", "ROOT", 20, 7, true, 3);
        assertEquals(4, lines.length);
        assertTrue(lines[1].contains("halved"));
        assertTrue(lines[3].endsWith("3"));
    }

    @Test
    void correctOrderOmitsTheHalvedNote() {
        assertTrue(!SweepDetails.lines("HELIX", "TRUNK", 20, 80, false, 1)[1].contains("halved"));
    }
}
