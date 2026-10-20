package dev.daweapon.type.theend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PadRulesTest {

    @Test
    void standingOnThePadCountsAndWalkingPastDoesNot() {
        assertTrue(PadRules.isOnPad(-562.4, 6.81, -314.4, -563, 6, -315));
        assertTrue(PadRules.isOnPad(-562.5, 7.4, -314.5, -563, 6, -315));
        assertFalse(PadRules.isOnPad(-560.5, 6.81, -314.4, -563, 6, -315));
        assertFalse(PadRules.isOnPad(-562.5, 6.1, -314.5, -563, 6, -315));
        assertFalse(PadRules.isOnPad(-562.5, 9.5, -314.5, -563, 6, -315));
    }

    @Test
    void arrivalOnTheOtherPadDoesNotBounceBack() {
        assertTrue(PadRules.coolingDown(10_000, 11_000));
        assertFalse(PadRules.coolingDown(10_000, 13_001));
    }
}
