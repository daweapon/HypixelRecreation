package net.swofty.type.skyblockgeneric.elections.perks;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.swofty.type.skyblockgeneric.elections.perks.ActivePerksTest.election;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MayorPerkStatisticsTest {

    @Test
    void wisdomPerksOnlyApplyOnPublicIslands() {
        ActivePerks perks = ActivePerks.of(election("COLE", List.of("MINING_XP_BUFF"), "FISHING_XP_BUFF"), null);

        ItemStatistics publicIsland = MayorPerkStatistics.flatBonuses(perks, true);
        ItemStatistics privateIsland = MayorPerkStatistics.flatBonuses(perks, false);

        assertEquals(60D, publicIsland.getOverall(ItemStatistic.MINING_WISDOM));
        assertEquals(50D, publicIsland.getOverall(ItemStatistic.FISHING_WISDOM));
        assertEquals(0D, privateIsland.getOverall(ItemStatistic.MINING_WISDOM));
        assertEquals(0D, privateIsland.getOverall(ItemStatistic.FISHING_WISDOM));
    }

    @Test
    void flatStatPerksStack() {
        ActivePerks perks = ActivePerks.of(election("BARRY", List.of("MAGIC_XP_BOOST"), "HUNTRESS_INTUITION"),
                SkyBlockMayor.MARINA);

        ItemStatistics bonuses = MayorPerkStatistics.flatBonuses(perks, false);

        assertEquals(15D, bonuses.getOverall(ItemStatistic.ALCHEMY_WISDOM));
        assertEquals(15D, bonuses.getOverall(ItemStatistic.ENCHANTING_WISDOM));
        assertEquals(10D, bonuses.getOverall(ItemStatistic.TRACKING));
        assertEquals(15D, bonuses.getOverall(ItemStatistic.SEA_CREATURE_CHANCE));
    }

    @Test
    void doubleTroubleTurnsSeaCreatureChanceIntoDoubleHookChance() {
        ActivePerks perks = ActivePerks.of(election("MARINA", List.of("DOUBLE_TROUBLE"), null), null);
        ItemStatistics total = ItemStatistics.builder()
                .withBase(ItemStatistic.SEA_CREATURE_CHANCE, 30D)
                .withBase(ItemStatistic.DOUBLE_HOOK_CHANCE, 2D)
                .build();

        ItemStatistics scaled = MayorPerkStatistics.applyScaling(perks, total, false);

        assertEquals(5D, scaled.getOverall(ItemStatistic.DOUBLE_HOOK_CHANCE), 1e-9);
    }

    @Test
    void pathfinderUsesTheWikiFormulaOnlyAgainstSlayerBosses() {
        ActivePerks perks = ActivePerks.of(election("AATROX", List.of("PATHFINDER"), null), null);
        ItemStatistics total = ItemStatistics.builder()
                .withBase(ItemStatistic.MAGIC_FIND, 100D)
                .withAdditivePercentage(ItemStatistic.MAGIC_FIND, 10D)
                .build();

        assertEquals(110D * 1.2D + 20D,
                MayorPerkStatistics.applyScaling(perks, total, true).getOverall(ItemStatistic.MAGIC_FIND), 1e-9);
        assertEquals(110D,
                MayorPerkStatistics.applyScaling(perks, total, false).getOverall(ItemStatistic.MAGIC_FIND), 1e-9);
    }

    @Test
    void statspocalypseRaisesCombatStatsByTenPercent() {
        ActivePerks perks = ActivePerks.of(election("JERRY", List.of("STATSPOCALYPSE"), null), null);
        ItemStatistics total = ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, 200D)
                .withBase(ItemStatistic.MINING_SPEED, 200D)
                .build();

        ItemStatistics scaled = MayorPerkStatistics.applyScaling(perks, total, false);

        assertEquals(220D, scaled.getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(200D, scaled.getOverall(ItemStatistic.MINING_SPEED), 1e-9);
    }
}
