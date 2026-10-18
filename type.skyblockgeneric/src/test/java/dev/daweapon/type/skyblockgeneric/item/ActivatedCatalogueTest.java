package dev.daweapon.type.skyblockgeneric.item;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivatedCatalogueTest {
    private static final Path CATALOGUE = Path.of("../configuration/skyblock/items/catalogue");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(String file) throws IOException {
        return (List<Map<String, Object>>) YamlFileUtils.loadYaml(CATALOGUE.resolve(file).toFile()).get("items");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> effects(Map<String, Object> item) {
        for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
            if ("ACCESSORY".equals(component.get("id"))) return (List<Map<String, Object>>) component.getOrDefault("effects", List.of());
        }
        return List.of();
    }

    @Test
    void onlyRustySpoonKeepsAnInactiveAccessoryEffectWithASpecificReason() throws IOException {
        int inactive = 0;
        for (Map<String, Object> item : items("accessories.yml")) {
            for (Map<String, Object> effect : effects(item)) {
                if (!"INACTIVE".equals(effect.get("type"))) continue;
                inactive++;
                assertEquals("RUSTY_SPOON", item.get("id"));
                assertFalse(String.valueOf(effect.get("reason")).contains("Not wired up"));
            }
        }
        assertEquals(1, inactive);
    }

    @Test
    void everyNotActiveNoteInTheWeaponsCatalogueGivesAConcreteReason() throws IOException {
        int notes = 0;
        for (Map<String, Object> item : items("weapons_tools.yml")) {
            for (Object line : (List<?>) item.getOrDefault("lore", List.of())) {
                String text = String.valueOf(line);
                if (!text.contains("Not active yet")) continue;
                notes++;
                assertTrue(text.contains("need") || text.contains("unconfirmed"), item.get("id") + ": " + text);
            }
        }
        assertEquals(14, notes);
    }

    @Test
    void activatedWeaponsNoLongerCarryTheNote() throws IOException {
        List<String> activated = List.of("FIG_AXE", "FIGSTONE_AXE", "HELIX_CHOPPER", "JUNGLE_AXE", "TREECAPITATOR_AXE",
                "DECENT_AXE", "SERIOUSLY_DAMAGED_AXE", "SCULPTORS_AXE", "WEATHER_STICK", "FLINT_SHOVEL", "STONK_PICKAXE", "BINGBOW");
        for (Map<String, Object> item : items("weapons_tools.yml")) {
            if (!activated.contains(String.valueOf(item.get("id")))) continue;
            assertFalse(String.valueOf(item.get("lore")).contains("Not active yet"), String.valueOf(item.get("id")));
        }
    }
}
