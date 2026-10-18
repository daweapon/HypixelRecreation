package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.type.skyblockgeneric.minion.freewill.FreeWillOdds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FreeWillOddsTest {
    @Test
    void stayChanceStartsAtFiftyAndGainsFourPerTier() {
        assertEquals(50, FreeWillOdds.stayChancePercent(1), 1e-9);
        assertEquals(54, FreeWillOdds.stayChancePercent(2), 1e-9);
        assertEquals(70, FreeWillOdds.stayChancePercent(6), 1e-9);
        assertEquals(94, FreeWillOdds.stayChancePercent(12), 1e-9);
    }

    @Test
    void stayChanceIsCappedAndNeverBelowTierOne() {
        assertEquals(100, FreeWillOdds.stayChancePercent(40), 1e-9);
        assertEquals(50, FreeWillOdds.stayChancePercent(0), 1e-9);
    }
}
