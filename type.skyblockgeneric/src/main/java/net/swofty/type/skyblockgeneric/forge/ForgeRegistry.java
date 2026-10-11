package net.swofty.type.skyblockgeneric.forge;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ForgeRegistry {
    public static final Path FILE = Path.of("./configuration/skyblock/forge/recipes.yml");
    private static volatile ForgeRegistry loaded;

    private final Map<String, ForgeRecipe> recipes;

    private ForgeRegistry(Map<String, ForgeRecipe> recipes) {
        this.recipes = recipes;
    }

    public static ForgeRegistry get() {
        ForgeRegistry current = loaded;
        if (current != null) return current;
        synchronized (ForgeRegistry.class) {
            if (loaded == null) loaded = load(FILE);
            return loaded;
        }
    }

    @SuppressWarnings("unchecked")
    public static ForgeRegistry load(Path file) {
        Map<String, Object> root;
        try (InputStream in = Files.newInputStream(file)) {
            root = new Yaml().load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
        Map<String, ForgeRecipe> recipes = new LinkedHashMap<>();
        for (Map<String, Object> entry : (List<Map<String, Object>>) root.get("recipes")) {
            Map<String, Integer> inputs = new LinkedHashMap<>();
            ((Map<String, Object>) entry.get("inputs")).forEach((key, amount) -> inputs.put(key, ((Number) amount).intValue()));
            ForgeRecipe recipe = new ForgeRecipe(
                    (String) entry.get("id"),
                    (String) entry.get("name"),
                    ForgeCategory.valueOf((String) entry.get("category")),
                    ((Number) entry.get("hotm")).intValue(),
                    (String) entry.get("requirement"),
                    ((Number) entry.get("seconds")).intValue(),
                    ((Number) entry.get("amount")).intValue(),
                    ((Number) entry.get("coins")).longValue(),
                    inputs);
            if (recipes.put(recipe.id(), recipe) != null) throw new IllegalStateException("Duplicate forge recipe " + recipe.id());
        }
        return new ForgeRegistry(recipes);
    }

    public ForgeRecipe recipe(String id) {
        return recipes.get(id);
    }

    public List<ForgeRecipe> all() {
        return List.copyOf(recipes.values());
    }

    public List<ForgeRecipe> inCategory(ForgeCategory category) {
        List<ForgeRecipe> list = new ArrayList<>();
        for (ForgeRecipe recipe : recipes.values()) if (recipe.category() == category) list.add(recipe);
        return list;
    }
}
