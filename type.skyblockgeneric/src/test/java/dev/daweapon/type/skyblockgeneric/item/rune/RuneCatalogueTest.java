package dev.daweapon.type.skyblockgeneric.item.rune;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.item.components.RuneComponent;
import dev.daweapon.type.skyblockgeneric.item.components.RuneableComponent;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuneCatalogueTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> runeItems() throws IOException {
        Map<String, Map<String, Object>> runes = new java.util.LinkedHashMap<>();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(new File(file.toString())).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> item = (Map<String, Object>) entry;
                    List<Map<String, Object>> components = (List<Map<String, Object>>) java.util.Objects.requireNonNullElse(item.get("components"), List.of());
                    if (components.stream().anyMatch(component -> "RUNE".equals(component.get("id")))) {
                        String id = String.valueOf(item.get("id"));
                        assertNull(runes.put(id, item), "Duplicate rune id " + id);
                    }
                }
            }
        }
        return runes;
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyRuneIsUniqueAndWellFormed() throws IOException {
        Map<String, Map<String, Object>> runes = runeItems();
        assertTrue(runes.size() >= 68, "Rune count " + runes.size());
        List<String> problems = new ArrayList<>();
        for (var entry : runes.entrySet()) {
            if (!entry.getKey().endsWith("_RUNE")) problems.add(entry.getKey());
            Map<String, Object> component = ((List<Map<String, Object>>) entry.getValue().get("components")).stream()
                    .filter(c -> "RUNE".equals(c.get("id"))).findFirst().orElseThrow();
            RuneableComponent.RuneApplicableTo.valueOf(String.valueOf(component.get("applicable_to")));
            assertTrue(((Number) component.get("required_level")).intValue() >= 0);
            assertTrue(String.valueOf(component.get("skull_texture")).matches("[0-9a-f]{60,64}"), entry.getKey() + " texture");
        }
        assertTrue(problems.isEmpty(), "Malformed ids " + problems);
    }

    @Test
    void everyRuneHasAnEffect() throws IOException {
        Set<String> missing = new HashSet<>(runeItems().keySet());
        missing.removeAll(RuneEffects.registeredRunes());
        assertTrue(missing.isEmpty(), "Runes without a particle effect: " + missing);
    }

    @Test
    void effectParticleCountScalesWithRuneLevel() {
        RuneEffect blood = RuneEffects.find("BLOOD_RUNE");
        assertNotNull(blood);
        assertEquals(3, blood.count(1));
        assertEquals(9, blood.count(3));
        assertEquals(3, blood.count(0));
    }

    @Test
    void applyLoreNamesTheApplicableItems() {
        assertEquals("<7>Apply this rune to throwing axes or", RuneComponent.applyLore(RuneableComponent.RuneApplicableTo.THROWING_AXES).getFirst());
        assertEquals("<7>Apply this rune to boots or", RuneComponent.applyLore(RuneableComponent.RuneApplicableTo.BOOTS).getFirst());
    }
}
