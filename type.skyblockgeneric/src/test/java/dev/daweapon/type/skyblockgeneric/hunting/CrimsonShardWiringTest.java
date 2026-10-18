package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonShardWiringTest {
    @BeforeAll
    static void requireConfiguration() {
        Assumptions.assumeTrue(Files.exists(Path.of("configuration/skyblock/shards.yml")),
                "the server configuration must be reachable at ./configuration");
    }

    private static AttributeDefinition.AttributeEffect effect(String id) {
        return AttributeRegistry.get(id).effects().getFirst();
    }

    @Test
    void fireEelGrantsTrophyChance() {
        assertEquals(AttributeDefinition.EffectType.STATISTIC_PER_LEVEL, effect("E14").type());
        assertEquals(ItemStatistic.TROPHY_FISH_CHANCE, effect("E14").statistic());
        assertEquals(20, effect("E14").atLevel(10), 1e-9);
    }

    @Test
    void taurusGrantsPercentageIntelligence() {
        assertEquals(AttributeDefinition.EffectType.PERCENTAGE_STATISTIC_PER_LEVEL, effect("L17").type());
        assertEquals(ItemStatistic.INTELLIGENCE, effect("L17").statistic());
        assertEquals(0.1, effect("L17").atLevel(1), 1e-9);
        assertEquals(1, effect("L17").atLevel(10), 1e-9);
    }

    @Test
    void lordJawbusGrantsDoubleHookOnCombatIslands() {
        assertEquals(ItemStatistic.DOUBLE_HOOK_CHANCE, effect("L26").statistic());
        assertEquals(AttributeDefinition.Condition.COMBAT_ISLAND, effect("L26").condition());
        assertEquals(5, effect("L26").atLevel(10), 1e-9);
    }

    @Test
    void lifelineTriggersOnlyBelowTwentyPercentHealth() {
        assertTrue(AttributeEffectService.lifelineTriggered(19, 100));
        assertFalse(AttributeEffectService.lifelineTriggered(20, 100));
        assertFalse(AttributeEffectService.lifelineTriggered(100, 100));
        assertFalse(AttributeEffectService.lifelineTriggered(0, 0));
    }

    @Test
    void essenceAndTeethShardsStayAbilityValues() {
        assertEquals(AttributeDefinition.EffectType.ABILITY_VALUE, effect("R26").type());
        assertEquals(AttributeDefinition.EffectType.ABILITY_VALUE, effect("R22").type());
        assertEquals(AttributeDefinition.EffectType.ABILITY_VALUE, effect("R36").type());
    }
}
