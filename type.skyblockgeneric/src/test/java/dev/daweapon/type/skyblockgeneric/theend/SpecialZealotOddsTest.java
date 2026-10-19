package dev.daweapon.type.skyblockgeneric.theend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialZealotOddsTest {
    private static final double TOLERANCE = 1e-9;

    private static double oneIn(double chancePercent) {
        return 100 / chancePercent;
    }

    @Test
    void zealotKillIsOneInFourHundredTwenty() {
        assertEquals(420, oneIn(SpecialZealotOdds.chancePercent(false, 0, 0)), TOLERANCE);
    }

    @Test
    void bruiserKillIsOneInThreeHundredEighty() {
        assertEquals(380.09, oneIn(SpecialZealotOdds.chancePercent(true, 0, 0)), 0.01);
    }

    @Test
    void legendaryEndermanPetIsOneInThreeHundredThirtySix() {
        assertEquals(336, oneIn(SpecialZealotOdds.chancePercent(false, 25, 0)), TOLERANCE);
    }

    @Test
    void zealuckTiersMultiplyTheChance() {
        double base = SpecialZealotOdds.chancePercent(false, 0, 0);
        double[] multipliers = {1.02, 1.04, 1.06, 1.08, 1.10};
        for (int tier = 1; tier <= 5; tier++) {
            double chance = SpecialZealotOdds.chancePercent(false, 0, SpecialZealotOdds.zealuckPercent(tier));
            assertEquals(base * multipliers[tier - 1], chance, TOLERANCE, "Zealuck " + tier);
        }
    }

    @Test
    void zealuckPercentIsTwoPerTierAndClamped() {
        assertEquals(0, SpecialZealotOdds.zealuckPercent(0), TOLERANCE);
        assertEquals(10, SpecialZealotOdds.zealuckPercent(5), TOLERANCE);
        assertEquals(10, SpecialZealotOdds.zealuckPercent(9), TOLERANCE);
    }

    @Test
    void everyBonusStacksMultiplicatively() {
        double expected = (1D / 420) * 1.105 * 1.25 * 1.10 * 100;
        assertEquals(expected, SpecialZealotOdds.chancePercent(true, 25, 10), TOLERANCE);
    }

    @Test
    void pityGuaranteesASpecialZealotAtFourHundredKills() {
        assertFalse(SpecialZealotOdds.pityReached(399));
        assertTrue(SpecialZealotOdds.pityReached(400));
        assertTrue(SpecialZealotOdds.pityReached(401));
    }
}
