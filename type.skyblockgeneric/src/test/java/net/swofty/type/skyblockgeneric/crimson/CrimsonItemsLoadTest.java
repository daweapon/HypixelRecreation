package net.swofty.type.skyblockgeneric.crimson;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonItemsLoadTest {
    private static final File DIRECTORY = new File("../configuration/skyblock/items/crimson");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void everyCrimsonItemParsesAndIsGenerated() throws Exception {
        for (Map<String, Object> item : items()) {
            ConfigurableSkyBlockItem parsed = ItemConfigParser.parseItem(item);
            assertNotNull(parsed, String.valueOf(item.get("id")));
            assertNotNull(ItemType.get((String) item.get("id")), item.get("id") + " is missing from ItemType");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyRecipeIngredientExists() throws Exception {
        List<String> missing = new ArrayList<>();
        for (Map<String, Object> item : items()) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                if (!"DEFAULT_CRAFTABLE".equals(component.get("id"))) continue;
                for (Map<String, Object> recipe : (List<Map<String, Object>>) component.get("recipes")) {
                    Map<String, Map<String, Object>> ingredients = (Map<String, Map<String, Object>>) recipe.get("ingredients");
                    ingredients.values().stream()
                            .map(ingredient -> (String) ingredient.get("type"))
                            .filter(type -> ItemType.get(type) == null)
                            .forEach(type -> missing.add(item.get("id") + " needs " + type));
                }
            }
        }
        assertTrue(missing.isEmpty(), String.join(", ", missing));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items() throws Exception {
        List<Map<String, Object>> all = new ArrayList<>();
        for (File file : Objects.requireNonNull(DIRECTORY.listFiles((_, name) -> name.endsWith(".yml")))) {
            all.addAll((List<Map<String, Object>>) YamlFileUtils.loadYaml(file).get("items"));
        }
        return all;
    }
}
