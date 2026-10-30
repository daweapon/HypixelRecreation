package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class MineshaftPityTest {
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
    void chanceIsOneInTheCounter() {
        assertEquals(0.05, MineshaftPity.chancePercent(2000, 0), 1e-9);
        assertEquals(100.0 / 859, MineshaftPity.chancePercent(859, 0), 1e-9);
        assertEquals(100, MineshaftPity.chancePercent(0, 0));
    }

    @Test
    void surveyorAndCoreRaiseTheChance() {
        assertEquals(0.05 * 1.2575, MineshaftPity.chancePercent(2000, 15.75 + MineshaftPity.CORE_BONUS_PERCENT), 1e-9);
        assertEquals(100, MineshaftPity.chancePercent(1, 500));
    }

    @Test
    void displayedPityCountsUp() {
        assertEquals(0, MineshaftPity.displayed(2000));
        assertEquals(859, MineshaftPity.displayed(1141));
        assertEquals(2000, MineshaftPity.displayed(0));
    }

    @Test
    void miningLowersTheCounterByBlockQuality() {
        assertEquals(1992, MineshaftPity.afterMining(2000, MineshaftBlocks.quality(MineableBlock.TITANIUM)));
        assertEquals(0, MineshaftPity.afterMining(5, 10));
    }

    @Test
    void spreadBlocksCountHalf() {
        int glacite = MineshaftBlocks.quality(MineableBlock.GLACITE_ORE, false);
        int spread = MineshaftBlocks.quality(MineableBlock.GLACITE_ORE, true);
        assertEquals(10, glacite + 3 * spread);
    }

    @Test
    void blockQualityTable() {
        assertEquals(2, MineshaftBlocks.quality(MineableBlock.MITHRIL_STRONG));
        assertEquals(8, MineshaftBlocks.quality(MineableBlock.TITANIUM));
        assertEquals(4, MineshaftBlocks.quality(MineableBlock.UMBER_ORE));
        assertEquals(4, MineshaftBlocks.quality(MineableBlock.TUNGSTEN_ORE));
        assertEquals(8, MineshaftBlocks.quality(MineableBlock.RUBY_ORE));
        assertEquals(10, MineshaftBlocks.quality(MineableBlock.ONYX_ORE));
        assertEquals(0, MineshaftBlocks.quality(MineableBlock.STONE));
        assertEquals(0, MineshaftBlocks.quality(null));
    }

    @Test
    void theCounterAtZeroAlwaysSpawnsAndResets() {
        MineshaftPity.Outcome outcome = MineshaftPity.step(null, 0, 4, 0, fixed(0.999999));
        assertTrue(outcome.spawned());
        assertEquals(MineshaftPity.START, outcome.counter());
    }

    @Test
    void aMissLowersTheCounter() {
        MineshaftPity.Outcome outcome = MineshaftPity.step(UUID.randomUUID(), 2000, 4, 0, fixed(0.5));
        assertFalse(outcome.spawned());
        assertEquals(1996, outcome.counter());
    }

    @Test
    void aLuckyRollSpawnsBeforeTheCounterRunsOut() {
        assertTrue(MineshaftPity.step(null, 2000, 4, 0, fixed(0.0001)).spawned());
        assertFalse(MineshaftPity.step(null, 2000, 4, 0, fixed(0.0006)).spawned());
    }

    @Test
    void noIncreaseWindowOnlyAppliesToTheSameServerForAMinute() {
        assertTrue(MineshaftPity.frozen(1_000, "mini1", "mini1", 30_000));
        assertFalse(MineshaftPity.frozen(1_000, "mini1", "mini1", 61_001));
        assertFalse(MineshaftPity.frozen(1_000, "mini1", "mini2", 30_000));
        assertFalse(MineshaftPity.frozen(0, null, "mini1", 30_000));
    }

    @Test
    void pityGuaranteesAPortalWithinTheStartingCounter() {
        SplittableRandom random = new SplittableRandom(7);
        int counter = MineshaftPity.START;
        int mined = 0;
        while (true) {
            mined++;
            MineshaftPity.Outcome outcome = MineshaftPity.step(null, counter, 4, 0, random);
            if (outcome.spawned()) break;
            counter = outcome.counter();
        }
        assertTrue(mined <= MineshaftPity.START / 4 + 1);
    }
}
