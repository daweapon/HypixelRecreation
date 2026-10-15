package dev.daweapon.type.skyblockgeneric.item;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivatedArmorCatalogueTest {
    private static final Path CATALOGUE = Path.of("../configuration/skyblock/items/catalogue");
    private static final Set<String> ACTIVATED_PREFIXES = Set.of("ANGLER_", "BACKWATER_", "SNORKELING_", "NUTCRACKER_",
            "WEREWOLF_", "EMBER_", "ARACHNE_");
    private static final Set<String> ACTIVATED = Set.of("CLOVER_HELMET", "RACING_HELMET", "MAGMA_FISH_HAT", "FISH_HAT",
            "GOLEM_HAT", "SALMON_BOOTS_NEW", "SALMON_LEGGINGS_NEW", "SALMON_CHESTPLATE_NEW", "TIKI_MASK", "MITHRIL_COAT",
            "OBSIDIAN_CHESTPLATE", "CHESTPLATE_OF_THE_PACK", "HELMET_OF_THE_PACK", "LEGGINGS_OF_THE_PACK");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(String file) throws IOException {
        return (List<Map<String, Object>>) YamlFileUtils.loadYaml(CATALOGUE.resolve(file).toFile()).get("items");
    }

    @Test
    void everyRemainingNoteGivesAConcreteReason() throws IOException {
        for (String file : List.of("armor_c.yml", "armor_d.yml")) {
            for (Map<String, Object> item : items(file)) {
                for (Object line : (List<?>) item.getOrDefault("lore", List.of())) {
                    String text = String.valueOf(line);
                    if (!text.contains("Not active yet")) continue;
                    assertTrue(text.contains("need"), item.get("id") + ": " + text);
                }
            }
        }
    }

    @Test
    void activatedArmourNoLongerCarriesTheNote() throws IOException {
        for (String file : List.of("armor_c.yml", "armor_d.yml")) {
            for (Map<String, Object> item : items(file)) {
                String id = String.valueOf(item.get("id"));
                boolean activated = ACTIVATED.contains(id) || ACTIVATED_PREFIXES.stream().anyMatch(id::startsWith);
                if (activated) assertFalse(String.valueOf(item.get("lore")).contains("Not active yet"), id);
            }
        }
    }
}
