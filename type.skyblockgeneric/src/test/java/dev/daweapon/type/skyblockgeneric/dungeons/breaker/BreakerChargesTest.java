package dev.daweapon.type.skyblockgeneric.dungeons.breaker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BreakerChargesTest {
    private static final long T = 1_000_000L;

    @Test
    void aNewDungeonbreakerHoldsTwentyCharges() {
        assertEquals(20, BreakerCharges.decode("", T).available(T));
        assertEquals(20, BreakerCharges.decode(null, T).available(T));
        assertEquals(20, BreakerCharges.decode("garbage@x", T).available(T));
    }

    @Test
    void eachBreakSpendsOneChargeAndAnEmptyItemBreaksNothing() {
        BreakerCharges charges = BreakerCharges.full(T);
        for (int i = 0; i < 20; i++) {
            charges = charges.spend(T);
            assertNotNull(charges);
        }
        assertEquals(0, charges.available(T));
        assertNull(charges.spend(T));
    }

    @Test
    void twoChargesComeBackEverySecondUpToTwenty() {
        BreakerCharges empty = new BreakerCharges(0, T);
        assertEquals(0, empty.available(T + 499));
        assertEquals(1, empty.available(T + 500));
        assertEquals(2, empty.available(T + 1_000));
        assertEquals(19, empty.available(T + 9_999));
        assertEquals(20, empty.available(T + 10_000));
        assertEquals(20, empty.available(T + 60_000));
    }

    @Test
    void spendingKeepsPartRegeneratedProgress() {
        BreakerCharges charges = new BreakerCharges(5, T).spend(T + 750);
        assertNotNull(charges);
        assertEquals(5, charges.charges());
        assertEquals(6, charges.available(T + 1_000));
    }

    @Test
    void spendingFromFullStartsTheRegenClockThen() {
        BreakerCharges spent = BreakerCharges.full(T).spend(T + 5_000);
        assertNotNull(spent);
        assertEquals(19, spent.available(T + 5_499));
        assertEquals(20, spent.available(T + 5_500));
    }

    @Test
    void echoesOfTheLostRestoresChargesButNeverPastTwenty() {
        BreakerCharges low = new BreakerCharges(3, T);
        assertEquals(8, low.restore(5, T).available(T));
        assertEquals(20, new BreakerCharges(18, T).restore(5, T).available(T));
        assertEquals(3, low.restore(0, T).available(T));
    }

    @Test
    void chargesSurviveBeingWrittenToTheItem() {
        BreakerCharges charges = new BreakerCharges(7, T);
        assertEquals(charges, BreakerCharges.decode(charges.encode(), T + 99_999));
        assertTrue(new BreakerCharges(25, T).charges() <= BreakerCharges.MAX);
    }

    @Test
    void loreShowsChargesLikeHypixel() {
        assertEquals(List.of("<7>Charges: <e>13<7>/<e>20<c>⸕"), Dungeonbreaker.lore(13));
    }
}
