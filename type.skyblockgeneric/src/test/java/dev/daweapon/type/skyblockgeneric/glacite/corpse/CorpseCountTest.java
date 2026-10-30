package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseCountTest {
    @Test
    void everyLayoutHasBetweenOneAndThreeBaseCorpses() {
        for (String code : CorpseAnchors.layoutCodes()) {
            int spots = CorpseAnchors.spots(code).size();
            int base = CorpseCount.base(code, spots);
            assertTrue(base >= 1 && base <= 3, code);
            assertTrue(base <= spots, code);
        }
    }

    @Test
    void theFairyShaftHasOneCorpseAndTheDenHasThree() {
        assertEquals(1, CorpseCount.base("FAIR_1", 1));
        assertEquals(3, CorpseCount.base("LITT_L", 4));
    }

    @Test
    void theBonusCorpseIsCappedAtFourAndBySpots() {
        assertEquals(4, CorpseCount.total(3, 5, true));
        assertEquals(3, CorpseCount.total(3, 5, false));
        assertEquals(1, CorpseCount.total(1, 1, true));
        assertEquals(4, CorpseCount.total(4, 6, true));
    }

    @Test
    void bonusChancesStackPerkAndMilestones() {
        assertEquals(25.5 + 20, CorpseCount.bonusChancePercent(25.5, 7), 0.0001);
        assertEquals(10, CorpseCount.bonusChancePercent(0, 2), 0.0001);
        assertEquals(10, CorpseCount.bonusChancePercent(0, 4), 0.0001);
        assertEquals(20, CorpseCount.bonusChancePercent(0, 5), 0.0001);
        assertEquals(40.2, CorpseCount.bonusRollPercent(20.2, 6), 0.0001);
        assertEquals(20.2, CorpseCount.bonusRollPercent(20.2, 2), 0.0001);
    }
}
