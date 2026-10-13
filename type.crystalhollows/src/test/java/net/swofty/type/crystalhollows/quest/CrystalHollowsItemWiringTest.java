package net.swofty.type.crystalhollows.quest;

import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress.Keeper;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalHollowsItemWiringTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path RECIPES = Path.of("../configuration/skyblock/forge/recipes.yml");
    private static final List<String> HOLLOWS_RECIPES = List.of(
            "DIVAN_DRILL", "DIVAN_HELMET", "DIVAN_CHESTPLATE", "DIVAN_LEGGINGS", "DIVAN_BOOTS", "DIVAN_PENDANT",
            "DIVAN_POWDER_COATING", "GEMSTONE_MIXTURE", "PERFECT_AMBER_GEM", "PERFECT_AMETHYST_GEM", "PERFECT_JADE_GEM",
            "PERFECT_SAPPHIRE_GEM", "PERFECT_TOPAZ_GEM", "PERFECT_RUBY_GEM", "PERFECT_JASPER_GEM", "GOBLIN_OMELETTE",
            "GOBLIN_OMELETTE_BLUE_CHEESE", "GOBLIN_OMELETTE_PESTO", "GOBLIN_OMELETTE_SPICY", "GOBLIN_OMELETTE_SUNNY_SIDE");

    @SuppressWarnings("unchecked")
    private static Set<String> itemIds() throws IOException {
        Set<String> ids = new HashSet<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                try (InputStream in = Files.newInputStream(file)) {
                    Map<String, Object> root = new Yaml().load(in);
                    if (root != null && root.get("items") instanceof List<?> list) {
                        for (Object entry : list) {
                            if (entry instanceof Map<?, ?> item && item.get("id") instanceof String id) ids.add(id);
                        }
                    }
                }
            }
        }
        return ids;
    }

    @Test
    void keepersAskForScavengedItemsThatExist() throws IOException {
        Set<String> ids = itemIds();
        for (Keeper keeper : Keeper.values()) {
            assertTrue(ids.contains(keeper.scavengedItemId), keeper + " wants missing item " + keeper.scavengedItemId);
        }
        assertTrue(ids.contains("JADE_CRYSTAL"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void crystalHollowsForgeRecipesResolveEveryItem() throws IOException {
        Set<String> ids = itemIds();
        Map<String, Object> root;
        try (InputStream in = Files.newInputStream(RECIPES)) {
            root = new Yaml().load(in);
        }
        List<Map<String, Object>> recipes = (List<Map<String, Object>>) root.get("recipes");
        Set<String> seen = new HashSet<>();
        for (Map<String, Object> recipe : recipes) {
            String id = (String) recipe.get("id");
            if (!HOLLOWS_RECIPES.contains(id)) continue;
            seen.add(id);
            assertTrue(ids.contains(id), "Forge output missing: " + id);
            for (String input : ((Map<String, Object>) recipe.get("inputs")).keySet()) {
                assertTrue(ids.contains(input), id + " needs missing item " + input);
            }
        }
        assertTrue(seen.containsAll(HOLLOWS_RECIPES), "Recipes not found: " + HOLLOWS_RECIPES.stream().filter(r -> !seen.contains(r)).toList());
    }
}
