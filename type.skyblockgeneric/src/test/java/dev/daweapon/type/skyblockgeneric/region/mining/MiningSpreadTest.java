package dev.daweapon.type.skyblockgeneric.region.mining;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MiningSpreadTest {
    @Test
    void zeroSpreadNeverSpreads() {
        assertEquals(0, MiningSpread.extraBlocks(0, 0));
    }

    @Test
    void fractionalSpreadIsAChanceAndWholeHundredsAreGuaranteed() {
        assertEquals(1, MiningSpread.extraBlocks(40, 0.39));
        assertEquals(0, MiningSpread.extraBlocks(40, 0.41));
        assertEquals(2, MiningSpread.extraBlocks(200, 0.99));
        assertEquals(3, MiningSpread.extraBlocks(250, 0.49));
    }

    @Test
    void thereAreTwentySixNeighbours() {
        assertEquals(26, MiningSpread.neighbourOffsets().size());
    }

    @Test
    void gemstonesUseGemstoneSpreadAndOresUseMiningSpread() {
        assertEquals(ItemStatistic.GEMSTONE_SPREAD, MiningSpread.statisticFor(MineableBlock.RUBY_ORE));
        assertEquals(ItemStatistic.MINING_SPREAD, MiningSpread.statisticFor(MineableBlock.ORE_COAL));
        assertEquals(ItemStatistic.MINING_SPREAD, MiningSpread.statisticFor(MineableBlock.MITHRIL_STRONG));
        assertNull(MiningSpread.statisticFor(MineableBlock.OAK_LOG));
        assertNull(MiningSpread.statisticFor(null));
    }

    @Test
    void pickReturnsNullWhenThereIsNoCandidate() {
        assertNull(MiningSpread.pick(List.<Point>of(), new Random(1)));
        assertEquals(new Vec(1, 2, 3), MiningSpread.pick(List.<Point>of(new Vec(1, 2, 3)), new Random(1)));
    }
}
