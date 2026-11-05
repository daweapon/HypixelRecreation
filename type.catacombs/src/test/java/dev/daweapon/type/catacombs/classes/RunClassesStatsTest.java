package dev.daweapon.type.catacombs.classes;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunClassesStatsTest {
    @Test
    void classBaseStatsAtLevelZero() {
        ItemStatistics mage = RunClasses.statistics(DungeonClassType.MAGE, 0, 1);
        assertEquals(250, mage.getOverall(ItemStatistic.INTELLIGENCE), 1e-9);
        ItemStatistics tank = RunClasses.statistics(DungeonClassType.TANK, 0, 1);
        assertEquals(100, tank.getOverall(ItemStatistic.HEALTH), 1e-9);
        assertEquals(10, tank.getOverall(ItemStatistic.VITALITY), 1e-9);
        ItemStatistics berserk = RunClasses.statistics(DungeonClassType.BERSERK, 0, 1);
        assertEquals(30, berserk.getOverall(ItemStatistic.SPEED), 1e-9);
    }

    @Test
    void loneClassDoublesTheStats() {
        ItemStatistics lone = RunClasses.statistics(DungeonClassType.MAGE, 50, 2);
        assertEquals(1_000, lone.getOverall(ItemStatistic.INTELLIGENCE), 1e-9);
        assertEquals(30, lone.getOverall(ItemStatistic.ABILITY_DAMAGE), 1e-9);
    }

    @Test
    void hitHistoryKeepsTheHighestHitOfTheLastMinute() {
        HitHistory history = new HitHistory();
        UUID player = UUID.randomUUID();
        history.record(player, 500, 0);
        history.record(player, 200, 30_000);
        assertEquals(500, history.highest(player, 59_000), 1e-9);
        assertEquals(200, history.highest(player, 61_000), 1e-9);
        assertEquals(0, history.highest(player, 200_000), 1e-9);
        assertEquals(0, history.highest(UUID.randomUUID(), 0), 1e-9);
    }
}
