package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeCatalogueTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items/hunting/attribute_shards.yml");
    private static final Set<String> TYPED_KEYS = Set.of(AttributeEffectService.KEY_RULER,
            AttributeEffectService.KEY_RESISTANCE, AttributeEffectService.KEY_FORTUNE);

    @BeforeAll
    static void requireConfiguration() {
        Assumptions.assumeTrue(Files.exists(Path.of("configuration/skyblock/shards.yml")),
                "the server configuration must be reachable at ./configuration");
    }

    private static String itemId(AttributeDefinition definition) {
        return "SHARD_" + definition.shard().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    @Test
    void everyRealAttributeIsCatalogued() {
        assertTrue(AttributeRegistry.values().size() >= 325);
    }

    @Test
    void idsMatchTheirRarityAndAreUnique() {
        Set<AttributeId> seen = new HashSet<>();
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            assertEquals(definition.id().rarity(), definition.rarity(), definition.id().toString());
            assertTrue(seen.add(definition.id()), "duplicate " + definition.id());
        }
    }

    @Test
    void shardNamesProduceUniqueItemIds() {
        Set<String> seen = new HashSet<>();
        for (AttributeDefinition definition : AttributeRegistry.values())
            assertTrue(seen.add(itemId(definition)), "duplicate item id " + itemId(definition));
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyAttributeHasExactlyOneShardItemWithTheDerivedId() throws IOException {
        Assumptions.assumeTrue(Files.exists(ITEMS));
        Map<String, String> itemByShardId = new HashMap<>();
        try (InputStream stream = Files.newInputStream(ITEMS)) {
            Map<String, Object> root = new Yaml().load(stream);
            for (Map<String, Object> item : (List<Map<String, Object>>) root.get("items")) {
                for (Map<String, Object> component : (List<Map<String, Object>>) item.get("components")) {
                    if (!"ATTRIBUTE_SHARD".equals(component.get("id"))) continue;
                    String previous = itemByShardId.put(component.get("shard_id").toString(), item.get("id").toString());
                    assertNull(previous, "two items for " + component.get("shard_id"));
                }
            }
        }
        for (AttributeDefinition definition : AttributeRegistry.values())
            assertEquals(itemId(definition), itemByShardId.get(definition.id().toString()), definition.id().toString());
        assertEquals(AttributeRegistry.values().size(), itemByShardId.size());
    }

    @Test
    void mobTypedEffectsUseTheTypedKeysAndCarryNoStatistic() {
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            for (AttributeDefinition.AttributeEffect effect : definition.effects()) {
                if (effect.mobType() == null) continue;
                assertTrue(TYPED_KEYS.contains(effect.key()), definition.id() + " key " + effect.key());
                assertNull(effect.statistic(), definition.id() + " would be applied as a flat statistic");
                assertEquals(AttributeDefinition.EffectType.ABILITY_VALUE, effect.type());
            }
        }
    }

    @Test
    void abilityEffectsNeverCarryAStatistic() {
        for (AttributeDefinition definition : AttributeRegistry.values())
            for (AttributeDefinition.AttributeEffect effect : definition.effects())
                if (effect.type() == AttributeDefinition.EffectType.ABILITY_VALUE)
                    assertNull(effect.statistic(), definition.id().toString());
    }

    @Test
    void humanoidRulerIsWetwingAndNoLongerRareDrowned() {
        AttributeDefinition wetwing = AttributeRegistry.get("U14");
        assertNotNull(wetwing);
        assertEquals("Wetwing", wetwing.shard());
        assertEquals(MobType.HUMANOID, wetwing.effects().getFirst().mobType());
        assertEquals("Undead Fortune", AttributeRegistry.get("R18").name());
    }

    @Test
    void conditionalStatisticShardsCarryTheirCondition() {
        AttributeDefinition cavernfish = AttributeRegistry.get("C41");
        AttributeDefinition.AttributeEffect effect = cavernfish.effects().getFirst();
        assertEquals(ItemStatistic.FISHING_SPEED, effect.statistic());
        assertEquals(AttributeDefinition.Condition.MINING_ISLAND, effect.condition());
        assertEquals(30, effect.atLevel(10), 1e-9);
    }

    @Test
    void fusionCostComesFromTheDefinition() {
        AttributeDefinition fly = AttributeRegistry.get("C13");
        assertEquals(5, AttributeFusionService.required(fly));
        AttributeDefinition treefrog = AttributeRegistry.get("U43");
        assertEquals(2, AttributeFusionService.required(treefrog));
        for (AttributeDefinition definition : AttributeRegistry.values())
            assertTrue(Set.of(0, 1, 2, 5).contains(definition.fusionCost()), definition.id().toString());
    }

    @Test
    void newShardsJoinIdFusion() {
        AttributeDefinition first = AttributeRegistry.get("C1");
        AttributeDefinition second = AttributeRegistry.get("C2");
        assertFalse(AttributeFusionService.results(first, second).isEmpty());
    }

    @Test
    void unavailableReasonsAreNeverBlank() {
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            if (definition.unavailable() == null) {
                assertTrue(definition.isAvailable());
                continue;
            }
            assertFalse(definition.unavailable().isBlank(), definition.id().toString());
            assertFalse(definition.isAvailable());
        }
    }
}
