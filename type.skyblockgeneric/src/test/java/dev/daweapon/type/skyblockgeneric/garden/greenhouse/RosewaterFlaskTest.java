package dev.daweapon.type.skyblockgeneric.garden.greenhouse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RosewaterFlaskTest {
    @Test
    void flaskGainsAStageEverySeventyFiveHoursAndEvolvesAtThreeHundred() {
        assertEquals(0, RosewaterFlask.bonusFor(0));
        assertEquals(0, RosewaterFlask.bonusFor(75 * 60 - 1));
        assertEquals(1, RosewaterFlask.bonusFor(75 * 60));
        assertEquals(3, RosewaterFlask.bonusFor(240 * 60));
        assertFalse(RosewaterFlask.hasEvolved(299 * 60));
        assertTrue(RosewaterFlask.hasEvolved(300 * 60));
        assertEquals(4, RosewaterFlask.bonusFor(10_000 * 60));
    }

    @Test
    void permanentFortuneStopsAtFiveFlasks() {
        assertEquals(0, RosewaterFlask.permanentFortune(0), 1e-9);
        assertEquals(3, RosewaterFlask.permanentFortune(3), 1e-9);
        assertEquals(5, RosewaterFlask.permanentFortune(5), 1e-9);
        assertEquals(5, RosewaterFlask.permanentFortune(9), 1e-9);
    }
}
