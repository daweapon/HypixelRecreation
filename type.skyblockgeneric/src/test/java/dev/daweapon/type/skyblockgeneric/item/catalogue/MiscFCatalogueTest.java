package dev.daweapon.type.skyblockgeneric.item.catalogue;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.consumable.VialOfVenom;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.EnchantLevelBoost;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiscFCatalogueTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path SHOPS = Path.of("../configuration/skyblock/shops");
    private static final Path FILE = ITEMS.resolve("catalogue/misc_f.yml");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itemsIn(Path file) throws Exception {
        Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
        return items instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    private static Set<String> knownItems() throws Exception {
        Set<String> known = new HashSet<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                itemsIn(file).forEach(item -> known.add(String.valueOf(item.get("id"))));
            }
        }
        return known;
    }

    private static Map<String, Object> item(String id) throws Exception {
        return itemsIn(FILE).stream().filter(item -> id.equals(item.get("id"))).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> component(String itemId, String componentId) throws Exception {
        for (Map<String, Object> component : (List<Map<String, Object>>) item(itemId).getOrDefault("components", List.of())) {
            if (componentId.equals(component.get("id"))) return component;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyRecipeIngredientIsAnItemThatExists() throws Exception {
        Set<String> known = knownItems();
        List<String> dangling = new ArrayList<>();
        for (Map<String, Object> item : itemsIn(FILE)) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                if (!"DEFAULT_CRAFTABLE".equals(component.get("id"))) continue;
                for (Map<String, Object> recipe : (List<Map<String, Object>>) component.get("recipes")) {
                    Object raw = recipe.get("ingredients");
                    Stream<Map<String, Object>> ingredients = raw instanceof Map<?, ?> map
                            ? ((Map<String, Map<String, Object>>) map).values().stream()
                            : ((List<Map<String, Object>>) raw).stream();
                    ingredients.filter(ingredient -> !ingredient.containsKey("minion"))
                            .map(ingredient -> String.valueOf(ingredient.get("type")))
                            .filter(type -> !type.equals("AIR") && !known.contains(type))
                            .forEach(type -> dangling.add(item.get("id") + " needs " + type));
                }
            }
        }
        assertTrue(dangling.isEmpty(), "Recipes with missing ingredients: " + dangling);
    }

    @Test
    void everyAnvilHandlerIsAnEnchantLevelBoost() throws Exception {
        for (String id : List.of("TROUBLED_BUBBLE", "TURBO_GOURD", "VIBRANT_CORAL", "WATER_HYACINTH")) {
            Map<String, Object> component = component(id, "ANVIL_COMBINABLE");
            assertNotNull(component, id);
            assertEquals(id, EnchantLevelBoost.valueOf(String.valueOf(component.get("handler_id"))).name());
        }
    }

    @Test
    void vialOfVenomIsConsumableAndMatchesItsLimits() throws Exception {
        assertEquals(VialOfVenom.ITEM_ID, component("VIAL_OF_VENOM", "INTERACTABLE").get("handler_id"));
        String lore = String.join("\n", (List<String>) item("VIAL_OF_VENOM").get("lore"));
        assertTrue(lore.contains("+1") && lore.contains("Crit Damage"));
        assertTrue(lore.contains("§a" + VialOfVenom.LIMIT));
        assertTrue(item("VIAL_OF_VENOM").get("unavailable") == null);
    }

    @Test
    void trueEssenceBrewsTrueResistance() throws Exception {
        assertEquals("TRUE_RESISTANCE", component("TRUE_ESSENCE", "BREWING_INGREDIENT").get("effect"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void gearKeepsTheWikiStatistics() throws Exception {
        Map<String, Object> chestplate = (Map<String, Object>) item("WITHER_CHESTPLATE").get("default_statistics");
        assertEquals(300, ((Number) chestplate.get("health")).intValue());
        assertEquals(250, ((Number) chestplate.get("defense")).intValue());
        Map<String, Object> stone = (Map<String, Object>) item("STONE_CHESTPLATE").get("default_statistics");
        assertEquals(130, ((Number) stone.get("defense")).intValue());
        assertTrue(item("STONE_CHESTPLATE").get("unavailable") == null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void tarantulaBroodfatherDropsTheVialAtTiersFourAndFive() throws Exception {
        Map<String, Object> root = YamlFileUtils.loadYaml(Path.of("../configuration/skyblock/slayers.yml").toFile());
        for (Map<String, Object> slayer : (List<Map<String, Object>>) root.get("slayers")) {
            if (!"TARANTULA_BROODFATHER".equals(slayer.get("id"))) continue;
            Map<String, Object> drop = ((List<Map<String, Object>>) slayer.get("drops")).stream()
                    .filter(entry -> "VIAL_OF_VENOM".equals(entry.get("item"))).findFirst().orElseThrow();
            Map<String, Object> tiers = (Map<String, Object>) drop.get("tiers");
            assertEquals(Set.of("IV", "V"), tiers.keySet());
            assertEquals(6, ((Number) drop.get("level")).intValue());
            return;
        }
        throw new AssertionError("no Tarantula Broodfather slayer");
    }

    @Test
    @SuppressWarnings("unchecked")
    void newShopsOnlyListRealItems() throws Exception {
        Set<String> known = knownItems();
        for (String shop : List.of("master_tactician_funk", "old_shaman_nyko")) {
            Map<String, Object> root = YamlFileUtils.loadYaml(SHOPS.resolve(shop + ".yml").toFile());
            for (Map<String, Object> entry : (List<Map<String, Object>>) root.get("entries")) {
                assertTrue(known.contains(String.valueOf(entry.get("item"))), shop + " sells " + entry.get("item"));
                Map<String, Object> cost = (Map<String, Object>) entry.get("cost");
                Object items = cost.get("items");
                if (items instanceof Map<?, ?> map) {
                    map.keySet().forEach(id -> assertTrue(known.contains(String.valueOf(id)), shop + " asks for " + id));
                }
            }
        }
    }
}
