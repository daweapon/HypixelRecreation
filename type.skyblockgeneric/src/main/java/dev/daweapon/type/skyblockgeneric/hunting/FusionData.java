package dev.daweapon.type.skyblockgeneric.hunting;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static dev.daweapon.type.skyblockgeneric.hunting.SpecialFusionRecipe.Alternative;
import static dev.daweapon.type.skyblockgeneric.hunting.SpecialFusionRecipe.Clause;

// Georik's Shards Fusion data from fusion.json: per-shard ID result, Chameleon results and
// family label, plus the special fusion recipes.
public final class FusionData {
    private static final FusionData INSTANCE = load();

    private final Map<AttributeId, Entry> entries;
    private final List<SpecialFusionRecipe> recipes;

    private FusionData(Map<AttributeId, Entry> entries, List<SpecialFusionRecipe> recipes) {
        this.entries = entries;
        this.recipes = recipes;
    }

    public static AttributeId idResult(AttributeId id) {
        Entry entry = INSTANCE.entries.get(id);
        return entry == null ? null : entry.idResult();
    }

    public static List<AttributeId> chameleon(AttributeId id) {
        Entry entry = INSTANCE.entries.get(id);
        return entry == null ? List.of() : entry.chameleon();
    }

    public static String family(AttributeId id) {
        Entry entry = INSTANCE.entries.get(id);
        return entry == null ? null : entry.family();
    }

    public static List<SpecialFusionRecipe> recipes() {
        return INSTANCE.recipes;
    }

    private record Entry(AttributeId idResult, List<AttributeId> chameleon, String family) {
    }

    @SuppressWarnings("unchecked")
    private static FusionData load() {
        Path path = Files.exists(Path.of("./configuration/skyblock/fusion.json"))
                ? Path.of("./configuration/skyblock/fusion.json")
                : Path.of("../configuration/skyblock/fusion.json");
        try (InputStream stream = Files.newInputStream(path)) {
            Map<String, Object> root = new Yaml().load(stream);
            Map<AttributeId, Entry> entries = new HashMap<>();
            Map<String, Map<String, Object>> shards = (Map<String, Map<String, Object>>) root.get("shards");
            for (Map.Entry<String, Map<String, Object>> shard : shards.entrySet()) {
                Map<String, Object> value = shard.getValue();
                List<AttributeId> chameleon = ((List<Object>) value.getOrDefault("chameleon", List.of())).stream()
                        .map(item -> AttributeId.parse(item.toString())).toList();
                entries.put(AttributeId.parse(shard.getKey()), new Entry(
                        value.get("id_result") == null ? null : AttributeId.parse(value.get("id_result").toString()),
                        chameleon, (String) value.get("family")));
            }
            List<SpecialFusionRecipe> recipes = new ArrayList<>();
            for (Map<String, Object> recipe : (List<Map<String, Object>>) root.get("special")) {
                List<List<Map<String, Object>>> inputs = (List<List<Map<String, Object>>>) recipe.get("inputs");
                recipes.add(new SpecialFusionRecipe(AttributeId.parse(recipe.get("output").toString()),
                        clause(inputs.get(0)), clause(inputs.get(1))));
            }
            return new FusionData(Map.copyOf(entries), List.copyOf(recipes));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read fusion file: " + path.toAbsolutePath(), exception);
        }
    }

    private static Clause clause(List<Map<String, Object>> alternatives) {
        return new Clause(alternatives.stream().map(FusionData::alternative).toList());
    }

    private static Alternative alternative(Map<String, Object> value) {
        return new Alternative(Boolean.TRUE.equals(value.get("any")),
                enumValue(AttributeRarity.class, value.get("rarity")), enumValue(AttributeRarity.class, value.get("minRarity")),
                enumValue(AttributeDefinition.AttributeCategory.class, value.get("category")), (String) value.get("family"),
                enumValue(AttributeDefinition.AttributeSkill.class, value.get("skill")),
                value.get("shard") == null ? null : AttributeId.parse(value.get("shard").toString()));
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, Object value) {
        return value == null ? null : Enum.valueOf(type, value.toString());
    }
}
