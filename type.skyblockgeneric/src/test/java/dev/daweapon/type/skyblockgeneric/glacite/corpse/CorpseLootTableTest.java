package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseLootTableTest {
    private final CorpseLootTable table = CorpseLootTable.get();

    @Test
    void poolTotalsMatchTheWiki() {
        assertEquals(16_750, table.totalWeight(CorpseType.LAPIS));
        assertEquals(6_640, table.totalWeight(CorpseType.UMBER));
        assertEquals(6_640, table.totalWeight(CorpseType.TUNGSTEN));
        assertEquals(3_515, table.totalWeight(CorpseType.VANGUARD));
    }

    @Test
    void rowCountsMatchTheWikiTables() {
        assertEquals(48, table.pool(CorpseType.LAPIS).size());
        assertEquals(38, table.pool(CorpseType.UMBER).size());
        assertEquals(39, table.pool(CorpseType.VANGUARD).size());
    }

    @Test
    void umberAndTungstenShareOnePoolAndOnlyLapisIsFree() {
        assertEquals(table.pool(CorpseType.UMBER), table.pool(CorpseType.TUNGSTEN));
        assertNull(table.rules(CorpseType.LAPIS).key());
        assertEquals(ItemType.UMBER_KEY, table.rules(CorpseType.UMBER).key());
        assertEquals(ItemType.TUNGSTEN_KEY, table.rules(CorpseType.TUNGSTEN).key());
        assertEquals(ItemType.SKELETON_KEY, table.rules(CorpseType.VANGUARD).key());
    }

    @Test
    void rollRangesAndDyeChancesMatchTheWiki() {
        assertEquals(3, table.rules(CorpseType.LAPIS).minRolls());
        assertEquals(6, table.rules(CorpseType.LAPIS).maxRolls());
        assertEquals(4, table.rules(CorpseType.UMBER).minRolls());
        assertEquals(7, table.rules(CorpseType.TUNGSTEN).maxRolls());
        assertEquals(5, table.rules(CorpseType.VANGUARD).minRolls());
        assertEquals(8, table.rules(CorpseType.VANGUARD).maxRolls());
        assertEquals(0.0004, table.rules(CorpseType.LAPIS).dyeChance());
        assertEquals(0.001, table.rules(CorpseType.UMBER).dyeChance());
        assertEquals(0.01, table.rules(CorpseType.VANGUARD).dyeChance());
    }

    @Test
    void everyMeterKeyOnAnEntryHasADropAndRequiredXp() {
        Set<String> ids = new HashSet<>();
        table.meterDrops().forEach(drop -> {
            assertTrue(ids.add(drop.id()), drop.id());
            assertTrue(drop.requiredXp() > 0, drop.id());
        });
        for (CorpseType type : CorpseType.values()) {
            table.pool(type).stream().map(CorpseLootTable.Entry::meterKey).filter(java.util.Objects::nonNull)
                    .forEach(key -> assertNotNull(table.meterDrop(key), key));
        }
        assertEquals(12_000, table.meterDrop("dead_mans_chestplate").requiredXp());
        assertEquals(5_000_000, table.meterDrop("frostbitten_dye").requiredXp());
        assertEquals(2_500_000, table.meterDrop("shattered_locket").requiredXp());
    }

    @Test
    void deadMansChestplateIsOnlyInUmberAndTungstenAtOnePointFivePercent() {
        assertTrue(table.poolHas(CorpseType.UMBER, "dead_mans_chestplate"));
        assertTrue(!table.poolHas(CorpseType.LAPIS, "dead_mans_chestplate"));
        assertTrue(!table.poolHas(CorpseType.VANGUARD, "dead_mans_chestplate"));
        double chance = table.variants(CorpseType.UMBER, "dead_mans_chestplate").getFirst().weight() / table.totalWeight(CorpseType.UMBER);
        assertEquals(0.01506, chance, 0.0001);
    }

    @Test
    void rollingFollowsTheWeights() {
        Random random = new Random(7);
        int rolls = 200_000;
        int scrap = 0;
        for (int i = 0; i < rolls; i++) {
            CorpseLootTable.Entry entry = table.roll(null, CorpseType.LAPIS, CorpseRngMeter.Boost.NONE, random);
            if (entry.item() == ItemType.SUSPICIOUS_SCRAP) scrap++;
        }
        assertEquals(500D / 16_750D, (double) scrap / rolls, 0.003);
    }
}
