package dev.daweapon.type.skyblockgeneric.minion;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.components.MinionComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewMinionTypesTest {
    private static final Map<MinionRegistry, Integer> NEW_MINIONS = Map.of(
            MinionRegistry.REVENANT, 12,
            MinionRegistry.TARANTULA, 12,
            MinionRegistry.VOIDLING, 11,
            MinionRegistry.SUNFLOWER, 12,
            MinionRegistry.LILY_PAD, 12);
    private static final Map<String, ConfigurableSkyBlockItem> PARSED = new HashMap<>();
    private static final Map<String, Map<String, Object>> YAML = new HashMap<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        Map<String, Object> root = YamlFileUtils.loadYaml(new File("../configuration/skyblock/items/minions.yml"));
        for (Map<String, Object> config : (List<Map<String, Object>>) root.get("items")) {
            ConfigurableSkyBlockItem parsed = ItemConfigParser.parseItem(config);
            YAML.put(String.valueOf(config.get("id")), config);
            if (parsed != null) PARSED.put(String.valueOf(config.get("id")), parsed);
        }
    }

    @Test
    void everyNewMinionHasIncreasingTiersAndTheWikiTierCount() {
        NEW_MINIONS.forEach((registry, tiers) -> {
            List<SkyBlockMinion.MinionTier> table = registry.asSkyBlockMinion().getTiers();
            assertEquals(tiers, table.size(), registry.name());
            for (int index = 0; index < table.size(); index++) {
                assertEquals(index + 1, table.get(index).tier(), registry.name());
                assertTrue(table.get(index).timeBetweenActions() > 0, registry.name());
                assertFalse(table.get(index).texture().isBlank(), registry.name());
            }
            assertEquals(tiers != 12, table.getLast().craftable(), registry.name());
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyNewMinionIsDefinedInYamlWithAnIngredientPerCraftableTier() {
        NEW_MINIONS.keySet().forEach(registry -> {
            ConfigurableSkyBlockItem item = PARSED.get(registry.getItemType().name());
            assertNotNull(item, registry.name());
            assertEquals(registry, item.getComponent(MinionComponent.class).getMinionRegistry());

            long craftable = registry.asSkyBlockMinion().getTiers().stream().filter(SkyBlockMinion.MinionTier::craftable).count();
            Map<String, Object> component = ((List<Map<String, Object>>) YAML.get(registry.getItemType().name()).get("components")).getFirst();
            assertEquals(craftable, ((List<String>) component.get("ingredients")).size(), registry.name());
            List<Map<String, Object>> tierRecipes = (List<Map<String, Object>>) component.getOrDefault("tier_recipes", List.of());
            for (Map<String, Object> recipe : tierRecipes) {
                assertTrue((int) recipe.get("tier") >= 1 && (int) recipe.get("tier") <= craftable, registry.name());
            }
        });
    }

    @Test
    void minionIngredientsParseWithAnExactTier() {
        MinionIngredient minion = MinionIngredient.parse(Map.of("minion", "ZOMBIE_MINION", "tier", 3));
        assertTrue(minion.isMinion());
        assertEquals(ItemType.ZOMBIE_MINION, minion.getItem());
        assertEquals(3, minion.getMinionTier());

        MinionIngredient plain = MinionIngredient.parse("REVENANT_FLESH:20");
        assertFalse(plain.isMinion());
        assertEquals(20, plain.getAmount());
    }

    @Test
    void everyNewMinionTierEarnsSkyBlockXp() {
        for (Map.Entry<MinionRegistry, Integer> entry : NEW_MINIONS.entrySet()) {
            List<Integer> tiers = entry.getKey().asSkyBlockMinion().getTiers().stream()
                    .map(SkyBlockMinion.MinionTier::tier).toList();
            var data = new DatapointMinionData.ProfileMinionData(new ArrayList<>(List.of(Map.entry(entry.getKey().name(), tiers))));
            int expected = 0;
            for (int tier : tiers) expected += MinionAwards.xpForTier(tier);
            assertEquals(expected, MinionAwards.skyBlockXp(data), entry.getKey().name());
            assertTrue(expected > 0, entry.getKey().name());
        }
    }
}
