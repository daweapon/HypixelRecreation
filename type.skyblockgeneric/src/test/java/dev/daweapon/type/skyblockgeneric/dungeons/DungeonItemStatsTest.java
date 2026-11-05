package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.debuff.VenomousDebuff;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonItemStatsTest {
    private static final double EPSILON = 1e-9;

    @Test
    void multiplierAddsCatacombsAndStarBonuses() {
        assertEquals(1.10, DungeonItemStats.multiplier(0, 0, 0, false), EPSILON);
        assertEquals(1.15, DungeonItemStats.multiplier(1, 0, 0, false), EPSILON);
        assertEquals(1.65, DungeonItemStats.multiplier(8, 0, 0, false), EPSILON);
        assertEquals(1 + 2.55 + 0.50, DungeonItemStats.multiplier(27, 5, 0, false), EPSILON);
        assertEquals(1 + 4.85 + 0.50, DungeonItemStats.multiplier(50, 5, 0, false), EPSILON);
        assertEquals(1 + 4.85 + 0.50, DungeonItemStats.multiplier(50, 10, 0, false), EPSILON);
    }

    @Test
    void masterStarsOnlyCountInMasterMode() {
        assertEquals(1 + 4.85 + 0.50, DungeonItemStats.multiplier(50, 5, 5, false), EPSILON);
        assertEquals(1 + 4.85 + 0.50 + 0.25, DungeonItemStats.multiplier(50, 5, 5, true), EPSILON);
    }

    @Test
    void bonusTurnsTheOutsideStatsIntoTheDungeonStats() {
        ItemStatistics unstarred = ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, 100D)
                .withBase(ItemStatistic.HEALTH_REGENERATION, 10D)
                .build();
        ItemStatistics outsideStars = ItemStatistics.multiply(unstarred, 5 * 0.02);
        double multiplier = DungeonItemStats.multiplier(20, 5, 0, false);

        ItemStatistics bonus = DungeonItemStats.bonus(unstarred, outsideStars, multiplier);
        ItemStatistics inDungeon = ItemStatistics.add(ItemStatistics.add(unstarred, outsideStars), bonus);

        assertEquals(100 * (1 + 1.85 + 0.50), inDungeon.getOverall(ItemStatistic.DAMAGE), EPSILON);
        assertEquals(10 * 1.10, inDungeon.getOverall(ItemStatistic.HEALTH_REGENERATION), EPSILON);
        assertEquals(DungeonItemStats.inDungeonValue(100, multiplier), inDungeon.getOverall(ItemStatistic.DAMAGE), EPSILON);
    }

    @Test
    void healthRegenAndVitalityAreNotScaled() {
        assertFalse(DungeonItemStats.scales(ItemStatistic.HEALTH_REGENERATION));
        assertFalse(DungeonItemStats.scales(ItemStatistic.VITALITY));
        assertTrue(DungeonItemStats.scales(ItemStatistic.STRENGTH));
    }

    @Test
    void catacombsHealthOnlyAppliesInsideTheCatacombs() {
        assertEquals(0D, CatacombsProgress.statistics(50, false).getOverall(ItemStatistic.HEALTH), EPSILON);
        assertEquals(100D, CatacombsProgress.statistics(50, true).getOverall(ItemStatistic.HEALTH), EPSILON);
        assertEquals(24D, CatacombsProgress.statistics(12, true).getOverall(ItemStatistic.HEALTH), EPSILON);
        assertEquals(0D, CatacombsProgress.statistics(0, true).getOverall(ItemStatistic.HEALTH), EPSILON);
    }

    @Test
    void venomousSlowsEightyPercentLessInTheCatacombs() {
        assertEquals(0.20, VenomousDebuff.speedReduction(7, false), EPSILON);
        assertEquals(0.04, VenomousDebuff.speedReduction(7, true), EPSILON);
    }

    @Test
    void theZoneFollowsTheSwitch() {
        try {
            CatacombsZone.use(() -> true);
            assertTrue(CatacombsZone.isInCatacombs());
            CatacombsZone.use(() -> false);
            assertFalse(CatacombsZone.isInCatacombs());
        } finally {
            CatacombsZone.useServerType();
        }
    }
}
