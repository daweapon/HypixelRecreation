package dev.daweapon.type.skyblockgeneric.hunting;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShardCraftingRecipesTest {
    @SuppressWarnings("unchecked")
    private static Map<String, Object> recipe(String id) throws IOException {
        Map<String, Object> root = new Yaml().load(Files.readString(Path.of("../configuration/skyblock/items/hunting/attribute_shards.yml")));
        for (Map<String, Object> item : (List<Map<String, Object>>) root.get("items")) {
            if (!id.equals(item.get("id"))) continue;
            for (Map<String, Object> component : (List<Map<String, Object>>) item.get("components")) {
                if ("DEFAULT_CRAFTABLE".equals(component.get("id")))
                    return ((List<Map<String, Object>>) component.get("recipes")).get(0);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static int amount(Map<String, Object> recipe, String letter, String type) {
        Map<String, Object> ingredient = ((Map<String, Map<String, Object>>) recipe.get("ingredients")).get(letter);
        assertEquals(type, ingredient.get("type"));
        return (Integer) ingredient.get("amount");
    }

    @Test
    void queenBeeUsesHoneycombHelixAndEggs() throws IOException {
        Map<String, Object> recipe = recipe("SHARD_QUEEN_BEE");
        assertNotNull(recipe);
        assertEquals(4, amount(recipe, "A", "ENCHANTED_HONEYCOMB"));
        assertEquals(16, amount(recipe, "B", "ENCHANTED_HELIX_LOG"));
        assertEquals(16, amount(recipe, "C", "ENCHANTED_EGG"));
    }

    @Test
    void honeyBadgerHawkAndJuneBugUseHelixLogsAroundTheirIngredient() throws IOException {
        Map<String, Object> badger = recipe("SHARD_HONEY_BADGER");
        assertEquals(8, amount(badger, "A", "ENCHANTED_HELIX_LOG"));
        assertEquals(1, amount(badger, "B", "ENCHANTED_HONEYCOMB"));
        Map<String, Object> hawk = recipe("SHARD_HAWK");
        assertEquals(4, amount(hawk, "B", "ENCHANTED_FEATHER"));
        Map<String, Object> bug = recipe("SHARD_JUNE_BUG");
        assertEquals(1, amount(bug, "B", "ENCHANTED_RUBY_VEILSHROOM"));
        assertEquals(16, amount(bug, "C", "ENCHANTED_EGG"));
    }
}
