package dev.daweapon.type.skyblockgeneric.hotm;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotmRegistryTest {
    private final HotmRegistry registry = HotmRegistry.load(Path.of("../configuration/skyblock/hotm"));

    @Test
    void perkCostsMatchTheWikiCumulativeTotals() {
        assertEquals(1_758_267L, registry.perk("mining_speed").totalCost(50));
        assertEquals(2_114_821L, registry.perk("mining_fortune").totalCost(50));
        assertEquals(917_130L, registry.perk("old_school").totalCost(20));
        assertEquals(4_644_659L, registry.perk("efficient_miner").totalCost(100));
        assertEquals(4_705_857L, registry.perk("pristine").totalCost(10));
    }

    @Test
    void everyPerkHasItsOwnCellInsideTheTree() {
        Set<String> cells = new HashSet<>();
        for (HotmPerk perk : registry.perks().values()) {
            assertTrue(perk.col() >= 1 && perk.col() <= 7, perk.id());
            assertTrue(perk.requires() == null || registry.perk(perk.requires()) != null, perk.id());
            assertTrue(perk.tier() >= 1 && perk.tier() <= 10, perk.id());
            assertTrue(cells.add(perk.tier() + ":" + perk.col()), perk.id());
        }
    }

    @Test
    void perksSitOnlyInCellsOfTheRealTree() {
        java.util.Map<Integer, java.util.Set<Integer>> cells = java.util.Map.of(
                1, Set.of(4), 2, Set.of(2, 3, 4, 5, 6), 3, Set.of(2, 4, 6), 4, Set.of(1, 2, 3, 4, 5, 6, 7),
                5, Set.of(2, 6), 6, Set.of(1, 2, 3, 4, 5, 6, 7), 7, Set.of(2, 4, 6), 8, Set.of(1, 2, 3, 4, 5, 6, 7),
                9, Set.of(2, 4, 6), 10, Set.of(1, 2, 3, 4, 5, 6, 7));
        for (HotmPerk perk : registry.perks().values()) {
            assertTrue(cells.get(perk.tier()).contains(perk.col()), perk.id() + " at tier " + perk.tier() + " col " + perk.col());
        }
        assertEquals(2, registry.perks().values().stream().filter(perk -> perk.tier() == 2 && perk.isAbility()).count());
        assertEquals(2, registry.perks().values().stream().filter(perk -> perk.tier() == 6 && perk.isAbility()).count());
        assertEquals(2, registry.perks().values().stream().filter(perk -> perk.tier() == 10 && perk.isAbility()).count());
    }

    @Test
    void coreOfTheMountainGrantsFiveTokensAndTiersGrantTwenty() {
        assertEquals(5, registry.coreLevels().stream().mapToInt(HotmCoreLevel::tokens).sum());
        assertEquals(20, java.util.stream.IntStream.rangeClosed(1, 10).map(tier -> registry.tierReward(tier).tokens()).sum());
        assertEquals(10, registry.maxCoreLevel());
    }

    @Test
    void perkEffectsScaleWithLevel() {
        assertEquals(1000, registry.perk("mining_speed").value(50), 0.0001);
        assertEquals(30, registry.perk("quick_forge").value(40), 0.0001);
        assertEquals(0, registry.perk("mining_speed").value(0), 0.0001);
    }

    @Test
    void newDataSurvivesASaveAndLoad() {
        DatapointHOTM datapoint = new DatapointHOTM("hotm");
        DatapointHOTM.PlayerHOTMData data = datapoint.getValue();
        data.setGlacitePowder(77);
        data.setCoreLevel(4);
        data.setSelectedAbility("PICKOBULUS");
        data.getPerkLevels().put("mining_speed", 12);
        data.getAbilityReadyAt().put("PICKOBULUS", 99L);
        DatapointHOTM restored = new DatapointHOTM("hotm");
        restored.deserializeValue(datapoint.getSerializedValue());
        assertEquals(77, restored.getValue().getGlacitePowder());
        assertEquals(4, restored.getValue().getCoreLevel());
        assertEquals("PICKOBULUS", restored.getValue().getSelectedAbility());
        assertEquals(12, restored.getValue().perkLevel("mining_speed"));
        assertEquals(99L, restored.getValue().getAbilityReadyAt().get("PICKOBULUS"));
    }

    @Test
    void oldSavesWithoutThePerkFieldsStillLoad() {
        DatapointHOTM restored = new DatapointHOTM("hotm");
        restored.deserializeValue("{\"experience\":5000,\"mithrilPowder\":10,\"gemstonePowder\":20,\"tokens\":3,\"tokensSpent\":1}");
        assertEquals(5000, restored.getValue().getExperience());
        assertEquals(0, restored.getValue().getCoreLevel());
        assertEquals(0, restored.getValue().perkLevel("mining_speed"));
    }
}
