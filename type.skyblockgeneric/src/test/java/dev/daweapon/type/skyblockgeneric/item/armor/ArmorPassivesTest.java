package dev.daweapon.type.skyblockgeneric.item.armor;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ArmorPassivesTest {
    @Test
    void lanternHelmetScalesWithFarmingLevel() {
        ItemStatistics stats = ArmorPassives.lanternHelmet(30, false);
        assertEquals(60.0, stats.getStatisticsBase().get(ItemStatistic.DEFENSE), 1e-9);
        assertEquals(120.0, stats.getStatisticsBase().get(ItemStatistic.HEALTH), 1e-9);
        assertFalse(stats.getStatisticsBase().containsKey(ItemStatistic.FARMING_FORTUNE));
    }

    @Test
    void lanternHelmetGivesFortuneOnlyWhileHoldingAnAxe() {
        ItemStatistics stats = ArmorPassives.lanternHelmet(30, true);
        assertEquals(30.0, stats.getStatisticsBase().get(ItemStatistic.FARMING_FORTUNE), 1e-9);
    }
}
