package dev.daweapon.type.skyblockgeneric.slayer.zombie;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.consumable.JerryCandy;
import dev.daweapon.type.skyblockgeneric.item.set.sets.ProspectingSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZombieSlayerGearTest {
    @Test
    void falchionsBoostUndeadDamageByTheirWikiAmounts() {
        assertEquals(1.5, ZombieSlayerGear.undeadBonus("REVENANT_FALCHION"), 1e-9);
        assertEquals(1.5, ZombieSlayerGear.undeadBonus("REVENANT_SWORD"), 1e-9);
        assertEquals(2, ZombieSlayerGear.undeadBonus("REAPER_FALCHION"), 1e-9);
        assertEquals(2, ZombieSlayerGear.undeadBonus("REAPER_SWORD"), 1e-9);
        assertEquals(0, ZombieSlayerGear.undeadBonus("UNDEAD_SWORD"), 1e-9);
        assertEquals(0, ZombieSlayerGear.undeadBonus(null), 1e-9);
    }

    @Test
    void onlyReaperBladesHealOnHit() {
        assertTrue(ZombieSlayerGear.healsOnHit("REAPER_FALCHION"));
        assertFalse(ZombieSlayerGear.healsOnHit("REVENANT_FALCHION"));
    }

    @Test
    void wardenGivesTwentyPercentPerTwentyFiveSpeedPlusOne() {
        assertEquals(1.61, ZombieSlayerGear.wardenBonus(200), 1e-9);
        assertEquals(0.01, ZombieSlayerGear.wardenBonus(0), 1e-9);
        assertEquals(0.01, ZombieSlayerGear.wardenBonus(-10), 1e-9);
    }

    @Test
    void jerryCandyGrantsTheWikiStats() {
        var stats = JerryCandy.statistics();
        assertEquals(100, stats.getOverall(ItemStatistic.HEALTH), 1e-9);
        assertEquals(20, stats.getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(2, stats.getOverall(ItemStatistic.FEROCITY), 1e-9);
        assertEquals(100, stats.getOverall(ItemStatistic.INTELLIGENCE), 1e-9);
        assertEquals(3, stats.getOverall(ItemStatistic.MAGIC_FIND), 1e-9);
        assertEquals(20 * 60 * 1000, JerryCandy.DURATION_MILLIS);
    }

    @Test
    void prospectingSetGrantsFortyMiningSpeed() {
        assertEquals(40, new ProspectingSet().getStatistics().getOverall(ItemStatistic.MINING_SPEED), 1e-9);
    }
}
