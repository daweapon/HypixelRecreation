package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonCollectionRewardsTest {
    private static final Set<String> CRIMSON_COLLECTIONS = Set.of("GLOWSTONE_DUST", "QUARTZ", "NETHERRACK", "MYCELIUM",
            "RED_SAND", "SULPHUR", "BLAZE_ROD", "MAGMA_CREAM", "GHAST_TEAR", "CHILI_PEPPER", "NETHER_WART");
    private static final Set<String> RECIPE_COMPONENTS = Set.of("DEFAULT_CRAFTABLE", "ENCHANTED", "MINION");

    @Test
    @SuppressWarnings("unchecked")
    void everyCrimsonRecipeUnlockPointsAtACraftableItem() throws Exception {
        Map<String, List<String>> components = componentsById();
        List<String> problems = new ArrayList<>();
        for (String file : List.of("mining.yml", "combat.yml", "farming.yml")) {
            Map<String, Object> data = YamlFileUtils.loadYaml(new File("../configuration/skyblock/collections", file));
            for (Map<String, Object> collection : (List<Map<String, Object>>) data.get("collections")) {
                if (!CRIMSON_COLLECTIONS.contains(collection.get("itemType"))) continue;
                for (Map<String, Object> tier : (List<Map<String, Object>>) collection.get("rewards")) {
                    for (Map<String, Object> reward : (List<Map<String, Object>>) tier.get("rewards")) {
                        if (!"RECIPE_UNLOCK".equals(reward.get("type"))) continue;
                        String id = (String) ((Map<String, Object>) reward.get("data")).get("unlockedItemType");
                        boolean craftable = components.getOrDefault(id, List.of()).stream().anyMatch(RECIPE_COMPONENTS::contains);
                        if (ItemType.get(id) == null || !craftable) problems.add(collection.get("itemType") + " -> " + id);
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), "Unlocks without a recipe: " + problems);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> componentsById() throws Exception {
        Map<String, List<String>> result = new HashMap<>();
        try (Stream<Path> files = Files.walk(Path.of("../configuration/skyblock/items"))) {
            for (Path path : files.filter(p -> p.toString().endsWith(".yml")).toList()) {
                Map<String, Object> data = YamlFileUtils.loadYaml(path.toFile());
                if (data == null || !(data.get("items") instanceof List<?> items)) continue;
                for (Object raw : items) {
                    Map<String, Object> item = (Map<String, Object>) raw;
                    List<String> ids = new ArrayList<>();
                    for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                        ids.add(String.valueOf(component.get("id")));
                    }
                    result.put(String.valueOf(item.get("id")), ids);
                }
            }
        }
        return result;
    }
}
