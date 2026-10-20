package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotfRegistryTest {
    private static final Path DIRECTORY = Path.of("../configuration/skyblock/hotf");
    private final HotfRegistry registry = HotfRegistry.load(DIRECTORY);

    @Test
    void theTreeHasThirtyFiveNodesOnTheRealGrid() {
        assertEquals(35, registry.perks().size());
        Map<Integer, Set<Integer>> cells = Map.of(
                1, Set.of(4), 2, Set.of(2, 3, 4, 5, 6), 3, Set.of(2, 4, 6), 4, Set.of(1, 2, 3, 4, 5, 6, 7),
                5, Set.of(2, 6), 6, Set.of(1, 2, 3, 4, 5, 6, 7), 7, Set.of(2, 4, 6), 8, Set.of(1, 2, 3, 4, 5, 6, 7));
        Set<String> seen = new HashSet<>();
        for (HotfPerk perk : registry.perks().values()) {
            assertTrue(cells.get(perk.tier()).contains(perk.col()), perk.id());
            assertTrue(seen.add(perk.tier() + ":" + perk.col()), perk.id());
        }
        assertEquals(3, registry.perks().values().stream().filter(HotfPerk::isAbility).count());
    }

    @Test
    void tierAndCenterTokensTotalSeventeen() {
        int tierTokens = registry.tiers().stream().mapToInt(HotfTierReward::tokens).sum();
        int coreTokens = registry.coreLevels().stream().mapToInt(HotfCoreLevel::tokens).sum();
        assertEquals(15, tierTokens);
        assertEquals(2, coreTokens);
        assertEquals(8, registry.maxTier());
        assertEquals(5, registry.maxCoreLevel());
    }

    @Test
    void tierThresholdsAndSkyBlockXpMatchTheWiki() {
        assertEquals(1, registry.tierOf(0));
        assertEquals(2, registry.tierOf(3_000));
        assertEquals(7, registry.tierOf(347_000));
        assertEquals(8, registry.tierOf(547_000));
        assertEquals(547_000L, registry.maxExperience());
        assertEquals(725, registry.tiers().stream().mapToInt(HotfTierReward::skyBlockXp).sum());
        assertEquals(250, registry.coreLevels().stream().mapToInt(HotfCoreLevel::skyBlockXp).sum());
    }

    @Test
    void perkCostsMatchTheWikiCumulativeTotals() {
        assertEquals(1_758_267L, registry.perk("sweep").totalCost(50));
        assertEquals(945_621L, registry.perk("luck_of_the_forest").totalCost(40));
        assertEquals(2_114_821L, registry.perk("foraging_fortune").totalCost(50));
        assertEquals(1_215_894L, registry.perk("collector").totalCost(50));
        assertEquals(1_215_894L, registry.perk("deep_waters").totalCost(50));
        assertEquals(3_683_370L, registry.perk("hunters_luck").totalCost(50));
        assertEquals(3_683_370L, registry.perk("galateas_might").totalCost(50));
        assertEquals(1_215_894L, registry.perk("iron_lungs").totalCost(50));
        assertEquals(945_621L, registry.perk("gifts_250").totalCost(40));
        assertEquals(110_682L, registry.perk("daily_wishes").totalCost(100));
        assertEquals(945_621L, registry.perk("forest_fisher").totalCost(40));
        assertEquals(2_544_096L, registry.perk("strength_boost").totalCost(50));
        assertEquals(2_544_096L, registry.perk("speed_boost").totalCost(50));
        assertEquals(4_644_659L, registry.perk("starlyn_supreme").totalCost(100));
        assertEquals(4_644_659L, registry.perk("efficient_forager").totalCost(100));
        assertEquals(3_551_741L, registry.perk("half_full").totalCost(25));
        assertEquals(3_551_741L, registry.perk("half_empty").totalCost(25));
        assertEquals(1_193_169L, registry.perk("ricochet").totalCost(10));
        assertEquals(7_733_838L, registry.perk("forest_speed").totalCost(50));
        assertEquals(7_733_838L, registry.perk("forest_strength").totalCost(50));
        assertEquals(3_683_370L, registry.perk("essence_fortune").totalCost(50));
        assertEquals(4_705_857L, registry.perk("timber").totalCost(10));
        assertEquals(2_423_618L, registry.perk("two_for_one").totalCost(100));
    }

    @Test
    void firstLevelCostsOnlyAToken() {
        assertEquals(0, registry.perk("sweep").costToReach(1));
        assertEquals(27, registry.perk("sweep").costToReach(2));
        assertEquals(1, registry.perk("half_full").tokenCost());
    }

    @Test
    void desertPerksFallBackToForestWhispersUntilTorrhusExists() {
        assertFalse(registry.desertWhispersEnabled());
        assertEquals(HotfCurrency.FOREST, registry.perk("forest_speed").currency());
        assertEquals(HotfCurrency.FOREST, registry.coreLevel(5).currency());
    }

    @Test
    void perkEffectsFollowTheWikiFormulas() {
        assertEquals(50, registry.perk("sweep").value(50));
        assertEquals(150, registry.perk("foraging_fortune").value(50));
        assertEquals(20, registry.perk("luck_of_the_forest").value(40));
        assertEquals(10, registry.perk("forest_fisher").value("main", 10));
        assertEquals(2, registry.perk("forest_fisher").value("second", 10));
        assertEquals(25, registry.perk("galateas_might").value(50));
        assertEquals(6.2, registry.perk("forest_speed").value(31), 1e-9);
        assertEquals(40_000, registry.perk("daily_wishes").value(100));
        assertEquals(15, registry.perk("efficient_forager").value(100));
    }

    @Test
    void halfFullAndHalfEmptyExcludeEachOther() {
        assertEquals("half_empty", registry.perk("half_full").exclusive());
        assertEquals("half_full", registry.perk("half_empty").exclusive());
    }

    @Test
    void everyUnavailablePerkSaysWhy() {
        for (HotfPerk perk : registry.perks().values()) {
            assertEquals(!perk.available(), perk.unavailable() != null, perk.id());
        }
    }

    @Test
    void playerDataSurvivesASerialisationRoundTrip() {
        DatapointHOTF.PlayerHOTFData data = new DatapointHOTF.PlayerHOTFData();
        data.setUnlocked(true);
        data.setCoreLevel(3);
        data.setBonusTokens(4);
        data.getPerkLevels().put("sweep", 12);
        data.getDisabledPerks().add("sweep");
        data.setSelectedAbility("DAMAGE_BOOST");
        data.setGiftsDay(20_000);
        data.setGiftsToday(7);
        data.setLotteryEffect("SWEEP");
        DatapointHOTF datapoint = new DatapointHOTF("hotf", data);
        DatapointHOTF.PlayerHOTFData copy = new DatapointHOTF("hotf").getSerializer().deserialize(datapoint.getSerializer().serialize(data));
        assertTrue(copy.isUnlocked());
        assertEquals(3, copy.getCoreLevel());
        assertEquals(4, copy.getBonusTokens());
        assertEquals(12, copy.perkLevel("sweep"));
        assertFalse(copy.isPerkActive("sweep"));
        assertEquals("DAMAGE_BOOST", copy.getSelectedAbility());
        assertEquals(7, copy.getGiftsToday());
        assertEquals("SWEEP", copy.getLotteryEffect());
    }
}
