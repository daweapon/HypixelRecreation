package dev.daweapon.type.skyblockgeneric.item.catalogue;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.reforge.Reforge;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueItemsTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Path CATALOGUE = ITEMS.resolve("catalogue");
    private static final Path REFORGES = Path.of("../configuration/skyblock/reforges");
    private static final List<String> RARITIES = List.of("common", "uncommon", "rare", "epic", "legendary", "mythic");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itemsIn(Path root) throws IOException {
        List<Map<String, Object>> all = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).sorted().toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (items instanceof List<?> list) list.forEach(entry -> all.add((Map<String, Object>) entry));
            }
        }
        return all;
    }

    @Test
    void everyCatalogueItemIsObtainableOrStatesWhyNot() throws IOException {
        List<String> missing = new ArrayList<>();
        for (Map<String, Object> item : itemsIn(CATALOGUE)) {
            boolean source = item.get("source") instanceof String s && !s.isBlank();
            boolean unavailable = item.get("unavailable") instanceof String s && !s.isBlank();
            if (!source && !unavailable) missing.add(String.valueOf(item.get("id")));
        }
        assertTrue(missing.isEmpty(), "Catalogue items with neither a source nor an unavailable reason: " + missing);
    }

    @Test
    void catalogueIdsAreDefinedOnce() throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        itemsIn(ITEMS).forEach(item -> counts.merge(String.valueOf(item.get("id")), 1, Integer::sum));
        List<String> duplicated = counts.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).sorted().toList();
        assertTrue(duplicated.isEmpty(), "Item ids defined more than once: " + duplicated);
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyReforgeStoneHasAReforgeWithFullRarityStats() throws IOException {
        int stones = 0;
        for (Map<String, Object> item : itemsIn(CATALOGUE)) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                if (!"REFORGE_STONE".equals(component.get("id"))) continue;
                stones++;
                String reforgeId = String.valueOf(component.get("reforge"));
                Map<String, Object> reforge = null;
                try (Stream<Path> files = Files.list(REFORGES)) {
                    for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                        Map<String, Object> data = YamlFileUtils.loadYaml(file.toFile());
                        String id = data.get("id") == null ? String.valueOf(data.get("name")) : String.valueOf(data.get("id"));
                        if (id.equalsIgnoreCase(reforgeId)) reforge = data;
                    }
                }
                assertNotNull(reforge, item.get("id") + " points at missing reforge " + reforgeId);
                assertEquals(Boolean.TRUE, reforge.get("stoneOnly"), reforgeId + " must be stone-only");
                Map<String, Map<String, Object>> stats = (Map<String, Map<String, Object>>) reforge.get("statistics");
                assertNotNull(stats, reforgeId + " has no statistics");
                stats.forEach((stat, values) -> {
                    ItemStatistic.valueOf(stat.toUpperCase());
                    assertEquals(Set.copyOf(RARITIES), values.keySet(), reforgeId + " " + stat + " rarities");
                });
            }
        }
        assertTrue(stones > 0);
    }

    @Test
    void itemRestrictedReforgesOnlyApplyToTheirItems() {
        Reforge warped = new Reforge("warped", "Warped", "Warped", Set.of(ReforgeType.SWORDS),
                Set.of("ASPECT_OF_THE_END"), true, (stats, level) -> stats);
        assertTrue(warped.isApplicableTo(ReforgeType.SWORDS, "ASPECT_OF_THE_END"));
        assertFalse(warped.isApplicableTo(ReforgeType.SWORDS, "DIAMOND_SWORD"));
        Reforge sharp = new Reforge("sharp", "Sharp", "Sharp", Set.of(ReforgeType.SWORDS), false, (stats, level) -> stats);
        assertTrue(sharp.isApplicableTo(ReforgeType.SWORDS, "DIAMOND_SWORD"));
    }
}
