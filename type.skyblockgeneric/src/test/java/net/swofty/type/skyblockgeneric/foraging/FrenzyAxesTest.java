package net.swofty.type.skyblockgeneric.foraging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FrenzyAxesTest {
    @Test
    void figAxeGainsOneSweepPerTwoThousandLogsCappedAtTwenty() {
        FrenzyAxes.Frenzy fig = FrenzyAxes.of("FIG_AXE");
        assertNotNull(fig);
        assertEquals(0, FrenzyAxes.bonusSweep(fig, 1_999));
        assertEquals(1, FrenzyAxes.bonusSweep(fig, 2_000));
        assertEquals(7, FrenzyAxes.bonusSweep(fig, 15_500));
        assertEquals(20, FrenzyAxes.bonusSweep(fig, 40_000));
        assertEquals(20, FrenzyAxes.bonusSweep(fig, 4_000_000));
    }

    @Test
    void figstoneSplitterNeedsTenThousandLogsPerSweep() {
        FrenzyAxes.Frenzy figstone = FrenzyAxes.of("FIGSTONE_AXE");
        assertEquals(0, FrenzyAxes.bonusSweep(figstone, 9_999));
        assertEquals(3, FrenzyAxes.bonusSweep(figstone, 30_000));
        assertEquals(20, FrenzyAxes.bonusSweep(figstone, 1_000_000));
    }

    @Test
    void helixChopperNeedsTwentyThousandLogsAndCapsAtForty() {
        FrenzyAxes.Frenzy helix = FrenzyAxes.of("HELIX_CHOPPER");
        assertEquals(1, FrenzyAxes.bonusSweep(helix, 20_000));
        assertEquals(40, FrenzyAxes.bonusSweep(helix, 800_000));
        assertEquals(40, FrenzyAxes.bonusSweep(helix, 9_000_000));
    }

    @Test
    void negativeLogCountsGiveNothingAndOtherAxesAreNotFrenzy() {
        assertEquals(0, FrenzyAxes.bonusSweep(FrenzyAxes.of("FIG_AXE"), -5));
        assertNull(FrenzyAxes.of("JUNGLE_AXE"));
        assertNull(FrenzyAxes.of(null));
    }
}
