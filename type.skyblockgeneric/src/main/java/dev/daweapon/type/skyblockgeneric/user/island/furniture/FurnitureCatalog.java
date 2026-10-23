package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.YamlFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FurnitureCatalog {
    private static final File FILE = new File("./configuration/skyblock/furniture_items.yml");
    private static Map<String, FurnitureDefinition> definitions;

    private FurnitureCatalog() {
    }

    public static synchronized FurnitureDefinition get(String itemId) {
        return all().get(itemId);
    }

    public static synchronized Map<String, FurnitureDefinition> all() {
        if (definitions == null) definitions = load(FILE);
        return definitions;
    }

    public static Collection<FurnitureDefinition> values() {
        return all().values();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, FurnitureDefinition> load(File file) {
        try {
            Map<String, Map<String, Object>> raw = (Map<String, Map<String, Object>>) YamlFileUtils.loadYaml(file).get("furniture");
            Map<String, FurnitureDefinition> loaded = new LinkedHashMap<>();
            raw.forEach((itemId, entry) -> loaded.put(itemId, parse(itemId, entry)));
            return loaded;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static FurnitureDefinition parse(String itemId, Map<String, Object> entry) {
        Map<String, Object> rawRecipe = (Map<String, Object>) entry.get("recipe");
        Map<String, Integer> recipe = null;
        if (rawRecipe != null) {
            recipe = new LinkedHashMap<>();
            for (Map.Entry<String, Object> ingredient : rawRecipe.entrySet()) {
                recipe.put(ingredient.getKey(), ((Number) ingredient.getValue()).intValue());
            }
        }
        return new FurnitureDefinition(
                itemId,
                ((Number) entry.getOrDefault("carpentry", 0)).intValue(),
                FurnitureInteraction.valueOf(String.valueOf(entry.getOrDefault("interaction", "NONE"))),
                ((Number) entry.getOrDefault("scale", 1.0)).doubleValue(),
                ((Number) entry.getOrDefault("width", 1.0)).floatValue(),
                ((Number) entry.getOrDefault("height", 1.0)).floatValue(),
                ((Number) entry.getOrDefault("seats", 0)).intValue(),
                recipe,
                ((Number) entry.getOrDefault("storage_pages", 0)).intValue()
        );
    }
}
