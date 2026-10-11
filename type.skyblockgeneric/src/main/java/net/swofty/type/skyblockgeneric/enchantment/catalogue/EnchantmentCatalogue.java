package net.swofty.type.skyblockgeneric.enchantment.catalogue;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Loads enchantments/catalogue.json, the Hypixel reference data every enchantment's text and metadata comes from.
public final class EnchantmentCatalogue {
    private static final String RESOURCE = "/enchantments/catalogue.json";
    private static final Map<String, CatalogueEntry> ENTRIES = load();

    private EnchantmentCatalogue() {}

    public static CatalogueEntry find(String key) {
        return ENTRIES.get(key);
    }

    public static CatalogueEntry require(String key) {
        CatalogueEntry entry = ENTRIES.get(key);
        if (entry == null) throw new IllegalArgumentException("No catalogue entry for enchantment " + key);
        return entry;
    }

    public static Map<String, CatalogueEntry> all() {
        return ENTRIES;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, CatalogueEntry> load() {
        LoaderOptions options = new LoaderOptions();
        options.setCodePointLimit(Integer.MAX_VALUE);
        try (InputStream stream = EnchantmentCatalogue.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing " + RESOURCE);
            Map<String, Map<String, Object>> raw = new Yaml(options).load(stream);
            Map<String, CatalogueEntry> entries = new LinkedHashMap<>();
            raw.forEach((key, data) -> {
                Map<Integer, String> levels = new TreeMap<>();
                ((Map<String, String>) data.get("levels")).forEach((level, text) -> levels.put(Integer.parseInt(level), text));
                entries.put(key, new CatalogueEntry(key, (String) data.get("name"), (Boolean) data.get("ultimate"),
                        (Integer) data.get("maxLevel"), levels, strings(data.get("sources")), strings(data.get("appliedTo")),
                        strings(data.get("conflicts")), ints(data.get("costs"))));
            });
            return Collections.unmodifiableMap(entries);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not read " + RESOURCE, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        return value == null ? List.of() : List.copyOf((List<String>) value);
    }

    private static List<Integer> ints(Object value) {
        if (value == null) return List.of();
        List<Integer> result = new ArrayList<>();
        for (Object item : (List<?>) value) result.add(((Number) item).intValue());
        return List.copyOf(result);
    }
}
