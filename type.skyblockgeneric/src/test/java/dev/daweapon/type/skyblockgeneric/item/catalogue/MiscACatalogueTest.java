package dev.daweapon.type.skyblockgeneric.item.catalogue;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MiscACatalogueTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path FILE = ITEMS.resolve("catalogue/misc_a.yml");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itemsIn(Path file) throws Exception {
        Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
        return items instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyRecipeIngredientIsAnItemThatExists() throws Exception {
        Set<String> known = new HashSet<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                itemsIn(file).forEach(item -> known.add(String.valueOf(item.get("id"))));
            }
        }
        List<String> dangling = new ArrayList<>();
        for (Map<String, Object> item : itemsIn(FILE)) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                if (!"DEFAULT_CRAFTABLE".equals(component.get("id"))) continue;
                for (Map<String, Object> recipe : (List<Map<String, Object>>) component.get("recipes")) {
                    Object ingredients = recipe.get("ingredients");
                    Collection<Map<String, Object>> entries = ingredients instanceof Map<?, ?> map
                            ? ((Map<String, Map<String, Object>>) map).values()
                            : (List<Map<String, Object>>) ingredients;
                    entries.stream()
                            .map(ingredient -> String.valueOf(ingredient.get("type")))
                            .filter(type -> !type.equals("AIR") && !known.contains(type))
                            .forEach(type -> dangling.add(item.get("id") + " needs " + type));
                }
            }
        }
        assertTrue(dangling.isEmpty(), "Recipes with missing ingredients: " + dangling);
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyItemModelComponentNamesAModel() throws Exception {
        for (Map<String, Object> item : itemsIn(FILE)) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                if ("ITEM_MODEL".equals(component.get("id"))) {
                    assertTrue(String.valueOf(component.get("item_model")).startsWith("hypixel_skyblock:"), item.get("id") + " model");
                }
            }
        }
    }
}
