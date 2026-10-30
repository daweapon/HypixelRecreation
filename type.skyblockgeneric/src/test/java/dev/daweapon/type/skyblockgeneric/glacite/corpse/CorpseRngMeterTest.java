package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseRngMeterTest {
    @Test
    void multiplierIsOneAtZeroTwoAtHalfAndThreeWhenFull() {
        assertEquals(1, CorpseRngMeter.multiplier(0, 60_000));
        assertEquals(2, CorpseRngMeter.multiplier(30_000, 60_000));
        assertEquals(3, CorpseRngMeter.multiplier(60_000, 60_000));
        assertEquals(3, CorpseRngMeter.multiplier(120_000, 60_000));
    }

    @Test
    void onlyTheSelectedDropIsBoostedAndFullMeterGuaranteesIt() {
        CorpseLootTable table = CorpseLootTable.get();
        CorpseRngMeter.Boost half = new CorpseRngMeter.Boost("refined_umber", 30_000, 60_000);
        CorpseLootTable.Entry refined = table.variants(CorpseType.UMBER, "refined_umber").getFirst();
        CorpseLootTable.Entry other = table.variants(CorpseType.UMBER, "refined_tungsten").getFirst();
        assertEquals(refined.weight() * 2, half.weight(refined), 0.0001);
        assertEquals(other.weight(), half.weight(other));
        assertFalse(half.guaranteed());
        assertTrue(new CorpseRngMeter.Boost("refined_umber", 60_000, 60_000).guaranteed());
        assertFalse(CorpseRngMeter.Boost.NONE.guaranteed());
        assertEquals(1, CorpseRngMeter.Boost.NONE.multiplier());
    }

    @Test
    void aFullMeterRollsTheSelectedItemEveryTimeThroughTheVariantPick() {
        CorpseLootTable table = CorpseLootTable.get();
        Random random = new Random(3);
        for (int i = 0; i < 200; i++) {
            assertEquals("glacite_amalgamation", table.rollVariant(null, CorpseType.VANGUARD, "glacite_amalgamation", random).meterKey());
        }
    }

    @Test
    void aBoostedDropAppearsMoreOftenThanItsBaseRate() {
        CorpseLootTable table = CorpseLootTable.get();
        CorpseRngMeter.Boost boost = new CorpseRngMeter.Boost("umber_plate", 150_000, 300_000);
        Random random = new Random(11);
        int rolls = 400_000;
        int hits = 0;
        for (int i = 0; i < rolls; i++) {
            if ("umber_plate".equals(table.roll(null, CorpseType.UMBER, boost, random).meterKey())) hits++;
        }
        double base = 5D / 6_640D;
        assertTrue((double) hits / rolls > base * 1.5);
    }

    @Test
    void percentCapsAtOneHundred() {
        assertEquals(50, CorpseRngMeter.percent(30_000, 60_000));
        assertEquals(100, CorpseRngMeter.percent(90_000, 60_000));
    }
}
