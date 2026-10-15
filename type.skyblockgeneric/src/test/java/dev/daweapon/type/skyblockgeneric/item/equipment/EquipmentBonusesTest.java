package dev.daweapon.type.skyblockgeneric.item.equipment;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentBonusesTest {
    private static final EquipmentBonuses.Situation NOWHERE = new EquipmentBonuses.Situation(null, null, false, false, null);

    private static Map<String, ItemStatistics> worn(String... ids) {
        Map<String, ItemStatistics> worn = new HashMap<>();
        for (String id : ids) worn.put(id, ItemStatistics.builder().withBase(ItemStatistic.HEALTH, 30.0).build());
        return worn;
    }

    @Test
    void mithrilEquipmentGrantsFortuneOnlyOnMithril() {
        List<String> ids = List.of("MITHRIL_CLOAK", "MITHRIL_BELT");
        assertEquals(10, EquipmentBonuses.blockFortune(ids, "MITHRIL_STRONG"));
        assertEquals(0, EquipmentBonuses.blockFortune(ids, "TITANIUM"));
        assertEquals(0, EquipmentBonuses.blockFortune(ids, "STONE"));
    }

    @Test
    void titaniumEquipmentGrantsFortuneOnMithrilAndTitanium() {
        List<String> ids = List.of("TITANIUM_CLOAK", "TITANIUM_NECKLACE", "TITANIUM_BELT");
        assertEquals(30, EquipmentBonuses.blockFortune(ids, "MITHRIL_WEAK"));
        assertEquals(30, EquipmentBonuses.blockFortune(ids, "TITANIUM"));
        assertEquals(0, EquipmentBonuses.blockFortune(ids, "COBBLESTONE"));
    }

    @Test
    void mixedMithrilAndTitaniumPiecesStack() {
        assertEquals(15, EquipmentBonuses.blockFortune(List.of("MITHRIL_CLOAK", "TITANIUM_BELT"), "MITHRIL_MEDIUM"));
    }

    @Test
    void fullMithrilSetGivesSpeedAndFortuneInTheDwarvenMinesOnly() {
        Map<String, ItemStatistics> worn = worn("MITHRIL_NECKLACE", "MITHRIL_CLOAK", "MITHRIL_BELT", "MITHRIL_GAUNTLET");
        ItemStatistics inMines = EquipmentBonuses.statistics(worn,
                new EquipmentBonuses.Situation(ServerType.SKYBLOCK_DWARVEN_MINES, null, false, false, null));
        assertEquals(30, inMines.getOverall(ItemStatistic.MINING_SPEED));
        assertEquals(5, inMines.getOverall(ItemStatistic.MINING_FORTUNE));
        assertTrue(EquipmentBonuses.statistics(worn, NOWHERE).getOverall(ItemStatistic.MINING_SPEED) == 0);
    }

    @Test
    void partialTitaniumSetGivesNoSetBonus() {
        Map<String, ItemStatistics> worn = worn("TITANIUM_NECKLACE", "TITANIUM_CLOAK", "TITANIUM_BELT");
        ItemStatistics stats = EquipmentBonuses.statistics(worn,
                new EquipmentBonuses.Situation(ServerType.SKYBLOCK_DWARVEN_MINES, null, false, false, null));
        assertEquals(0, stats.getOverall(ItemStatistic.MINING_SPEED));
    }

    @Test
    void fullTitaniumSetGivesFiftySpeedAndFortyFortune() {
        Map<String, ItemStatistics> worn = worn("TITANIUM_NECKLACE", "TITANIUM_CLOAK", "TITANIUM_BELT", "TITANIUM_GAUNTLET");
        ItemStatistics stats = EquipmentBonuses.statistics(worn,
                new EquipmentBonuses.Situation(ServerType.SKYBLOCK_DWARVEN_MINES, null, false, false, null));
        assertEquals(50, stats.getOverall(ItemStatistic.MINING_SPEED));
        assertEquals(40, stats.getOverall(ItemStatistic.MINING_FORTUNE));
    }

    @Test
    void enderEquipmentDoublesItsStatsOnTheEndAndDragonfadeTriplesThem() {
        EquipmentBonuses.Situation end = new EquipmentBonuses.Situation(ServerType.SKYBLOCK_THE_END, null, false, false, null);
        assertEquals(30, EquipmentBonuses.statistics(worn("ENDER_NECKLACE"), end).getOverall(ItemStatistic.HEALTH));
        assertEquals(60, EquipmentBonuses.statistics(worn("DRAGONFADE_CLOAK"), end).getOverall(ItemStatistic.HEALTH));
        assertEquals(0, EquipmentBonuses.statistics(worn("ENDER_NECKLACE"), NOWHERE).getOverall(ItemStatistic.HEALTH));
    }

    @Test
    void zorrosCapeDoublesDuringAContest() {
        EquipmentBonuses.Situation contest = new EquipmentBonuses.Situation(null, null, true, false, null);
        assertEquals(30, EquipmentBonuses.statistics(worn("ZORROS_CAPE"), contest).getOverall(ItemStatistic.HEALTH));
        assertEquals(0, EquipmentBonuses.statistics(worn("ZORROS_CAPE"), NOWHERE).getOverall(ItemStatistic.HEALTH));
    }

    @Test
    void shriveledBraceletOnlyAgainstArthropods() {
        EquipmentBonuses.Situation spider = new EquipmentBonuses.Situation(null, null, false, true, null);
        ItemStatistics stats = EquipmentBonuses.statistics(worn("SHRIVELED_BRACELET"), spider);
        assertEquals(5, stats.getOverall(ItemStatistic.COMBAT_WISDOM));
        assertEquals(10, stats.getOverall(ItemStatistic.MAGIC_FIND));
        assertEquals(0, EquipmentBonuses.statistics(worn("SHRIVELED_BRACELET"), NOWHERE).getOverall(ItemStatistic.MAGIC_FIND));
    }

    @Test
    void peltBeltGivesSpeedInTheMushroomDesert() {
        EquipmentBonuses.Situation desert = new EquipmentBonuses.Situation(null, RegionType.MUSHROOM_DESERT, false, false, null);
        assertEquals(30, EquipmentBonuses.statistics(worn("PELT_BELT"), desert).getOverall(ItemStatistic.SPEED));
        assertEquals(0, EquipmentBonuses.statistics(worn("PELT_BELT"), NOWHERE).getOverall(ItemStatistic.SPEED));
    }

    @Test
    void dragonfuseGloveUpgradesTheAspectOfTheDragons() {
        EquipmentBonuses.Situation holding = new EquipmentBonuses.Situation(null, null, false, false, EquipmentBonuses.ASPECT_OF_THE_DRAGONS);
        ItemStatistics stats = EquipmentBonuses.statistics(worn("DRAGONFUSE_GLOVE"), holding);
        assertEquals(35, stats.getOverall(ItemStatistic.DAMAGE));
        assertEquals(50, stats.getOverall(ItemStatistic.STRENGTH));
        assertEquals(0, EquipmentBonuses.statistics(worn("DRAGONFUSE_GLOVE"), NOWHERE).getOverall(ItemStatistic.STRENGTH));
    }

    @Test
    void knuckleSandwichBoostsFistsOnly() {
        assertTrue(EquipmentBonuses.fistsBoosted(List.of("KNUCKLE_SANDWICH"), true));
        assertFalse(EquipmentBonuses.fistsBoosted(List.of("KNUCKLE_SANDWICH"), false));
        assertFalse(EquipmentBonuses.fistsBoosted(List.of("MITHRIL_CLOAK"), true));
        assertEquals(6, EquipmentBonuses.KNUCKLE_SANDWICH_FIST_MULTIPLIER);
    }
}
