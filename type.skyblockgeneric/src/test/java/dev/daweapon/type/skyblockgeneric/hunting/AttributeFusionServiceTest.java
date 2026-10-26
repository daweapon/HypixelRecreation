package dev.daweapon.type.skyblockgeneric.hunting;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeFusionServiceTest {
    private static List<String> ids(AttributeDefinition first, AttributeDefinition second) {
        return AttributeFusionService.results(first, second).stream().map(r -> r.definition().id().toString()).toList();
    }

    private static AttributeDefinition shard(String id) {
        AttributeDefinition definition = AttributeRegistry.get(id);
        assertNotNull(definition, id);
        return definition;
    }

    private static AttributeDefinition byName(String name) {
        return AttributeRegistry.findByShard(name).orElseThrow();
    }

    @Test
    void ninetyNineSpecialRecipesLoadAndPointAtRealShards() {
        assertEquals(99, FusionData.recipes().size());
        for (SpecialFusionRecipe recipe : FusionData.recipes())
            assertNotNull(AttributeRegistry.get(recipe.output()), recipe.output().toString());
    }

    @Test
    void idResultsAndChameleonResultsPointAtRealShards() {
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            AttributeId result = FusionData.idResult(definition.id());
            if (result != null) assertNotNull(AttributeRegistry.get(result), definition.id().toString());
            for (AttributeId id : FusionData.chameleon(definition.id()))
                assertNotNull(AttributeRegistry.get(id), definition.id().toString());
        }
    }

    @Test
    void idFusionOffersTheIdResultsOfDifferentCategories() {
        assertEquals(List.of("C4", "C5"), ids(shard("C1"), shard("C2")));
    }

    @Test
    void sameShardCanBeFusedWithItself() {
        assertEquals(List.of("C10"), ids(shard("C7"), shard("C7")));
    }

    @Test
    void sameRarityAndCategoryUsesTheSecondShardsIdResult() {
        AttributeDefinition first = shard("C7");
        AttributeDefinition second = shard("C10");
        assertEquals(List.of(FusionData.idResult(second.id()).toString()), ids(first, second).stream()
                .filter(id -> id.equals(FusionData.idResult(second.id()).toString())).toList());
    }

    @Test
    void fusionNeverReturnsItsInputs() {
        List<AttributeDefinition> sample = AttributeRegistry.values().stream().limit(120).toList();
        for (AttributeDefinition a : sample)
            for (AttributeDefinition b : sample)
                for (AttributeFusionService.FusionResult result : AttributeFusionService.results(a, b)) {
                    assertFalse(result.definition().id().equals(a.id()) || result.definition().id().equals(b.id()));
                    assertTrue(AttributeFusionService.results(a, b).size() <= 3);
                }
    }

    @Test
    void specialFusionYieldsTwoShards() {
        List<AttributeFusionService.FusionResult> results = AttributeFusionService.results(byName("Queen Ant"), byName("King Cobra"));
        AttributeFusionService.FusionResult anteater = results.stream()
                .filter(r -> r.definition().shard().equals("Anteater")).findFirst().orElseThrow();
        assertEquals(2, anteater.amount());
        assertEquals(AttributeFusionService.FusionType.SPECIAL, anteater.type());
    }

    @Test
    void elementalRarityRuleMatchesACommonForestShardWithAnUncommonOrBetterPartner() {
        assertTrue(AttributeFusionService.results(shard("C46"), shard("U5")).stream().anyMatch(r ->
                r.definition().id().toString().equals("C1") && r.type() == AttributeFusionService.FusionType.SPECIAL));
        assertTrue(AttributeFusionService.results(shard("C46"), shard("C58")).stream()
                .noneMatch(r -> r.definition().id().toString().equals("C1")));
    }

    @Test
    void galaxyFishNeedsTwoSunFish() {
        AttributeDefinition sunFish = byName("Sun Fish");
        assertTrue(AttributeFusionService.results(sunFish, sunFish).stream()
                .anyMatch(r -> r.definition().shard().equals("Galaxy Fish") && r.amount() == 2));
        assertEquals(sunFish.fusionCost() > 0 ? sunFish.fusionCost() * 2 : AttributeFusionService.required(sunFish) * 2,
                AttributeFusionService.requiredOf(sunFish, sunFish, sunFish));
    }

    @Test
    void chameleonOffersTheSheetsThreeResults() {
        AttributeDefinition chameleon = byName("Chameleon");
        assertEquals(List.of("R64", "R65", "R66"), ids(chameleon, shard("R63")));
        assertEquals(List.of("R64", "R65", "R66"), ids(shard("R63"), chameleon));
        assertTrue(AttributeFusionService.results(chameleon, shard("R63")).stream().allMatch(r -> r.amount() == 1));
        assertEquals(1, AttributeFusionService.required(chameleon));
    }

    @Test
    void chameleonCrossesRarityAndEndsEmptyAtTheLastLegendary() {
        AttributeDefinition chameleon = byName("Chameleon");
        assertTrue(ids(chameleon, shard("E36")).contains("L1"));
        assertTrue(ids(chameleon, shard("L47")).isEmpty());
    }

    @Test
    void huntingXpFollowsTheWikiTable() {
        AttributeDefinition common = shard("C1");
        AttributeDefinition legendary = shard("L1");
        assertEquals(75, HuntingXp.hunt(common), 1e-9);
        assertEquals(750, HuntingXp.charm(common), 1e-9);
        assertEquals(18.75, HuntingXp.fusion(common), 1e-9);
        assertEquals(10_000, HuntingXp.charm(legendary), 1e-9);
        assertEquals(250, HuntingXp.fusion(legendary), 1e-9);
    }

    @Test
    void glaciteWalkersAndGhostsGiveHalfTheHuntingXp() {
        assertEquals(150, HuntingXp.hunt(byName("Glacite Walker")), 1e-9);
        assertEquals(250, HuntingXp.hunt(byName("Ghost")), 1e-9);
        assertEquals(2500, HuntingXp.charm(byName("Ghost")), 1e-9);
    }
}
