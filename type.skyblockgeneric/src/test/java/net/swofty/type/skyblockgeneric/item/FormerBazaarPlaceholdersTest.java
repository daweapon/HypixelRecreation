package net.swofty.type.skyblockgeneric.item;

import net.swofty.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormerBazaarPlaceholdersTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final List<String> FILES = List.of("mining/dwarven.yml", "mining/vanilla.yml",
            "miscellaneous/fishing_bazaar_placeholders.yml", "farming/farming.yml", "combat/bazaar_placeholders.yml");
    private static final Set<String> FORMER_PLACEHOLDERS = Set.of(
            "AMALGAMATED_CRIMSONITE_NEW", "ARACHNE_FANG", "ARACHNE_FRAGMENT", "CRYSTAL_FRAGMENT",
            "DUNGEON_CHEST_KEY", "DUNGEON_DECOY", "DUNGEON_TRAP", "FEL_PEARL",
            "GOLDEN_POWDER", "HEALING_TISSUE", "HOLY_FRAGMENT", "INFLATABLE_JERRY",
            "KUUDRA_TEETH", "MIMIC_FRAGMENT", "MITE_GEL", "MOLTEN_POWDER",
            "NETHER_STAR", "OLD_FRAGMENT", "POISON_SAMPLE", "PROTECTOR_FRAGMENT",
            "RITUAL_RESIDUE", "SPIRIT_BONE", "SPIRIT_LEAP", "SPIRIT_WING",
            "STRONG_FRAGMENT", "SUPERIOR_FRAGMENT", "TARANTULA_CATALYST", "UNSTABLE_FRAGMENT",
            "WEAK_WOLF_CATALYST", "WISE_FRAGMENT", "WITHER_CATALYST", "YOUNG_FRAGMENT",
            "CONDENSED_FERMENTO", "CROPIE", "ENCHANTED_CARROT_ON_A_STICK", "ENCHANTED_COOKED_RABBIT",
            "FERMENTO", "FINE_FLOUR", "SQUASH", "WRIGGLING_LARVA",
            "BEJEWELED_HANDLE", "DRILL_ENGINE", "GLACITE_AMALGAMATION", "GOLDEN_PLATE",
            "JUNGLE_HEART", "MITHRIL_PLATE", "PERFECT_PLATE", "PRECURSOR_APPARATUS",
            "REFINED_TUNGSTEN", "REFINED_UMBER", "SKELETON_KEY", "SUSPICIOUS_SCRAP",
            "TUNGSTEN_KEY", "TUNGSTEN_PLATE", "UMBER_KEY", "UMBER_PLATE",
            "YOGGIE", "REFINED_DIAMOND", "BAYOU_WATER_ORB", "BLUE_ICE_HUNK",
            "BOBBIN_SCRIPTURES", "BRIMSTONE_HANDLE", "CUP_OF_BLOOD", "EMPTY_CHUM_BUCKET",
            "EXALTED_LUSHLILAC_BONBON", "FLAMING_HEART", "FRIED_FEATHER", "FULL_CHUM_BUCKET",
            "HORN_OF_TAURUS", "HOTSPOT_WATER_ORB", "ICE_HUNK", "LAVA_SHELL",
            "LAVA_WATER_ORB", "LUMP_OF_MAGMA", "LUSHLILAC_BONBON", "MAGMA_LORD_FRAGMENT",
            "MOOGMA_PELT", "ORB_OF_ENERGY", "PRIME_LUSHLILAC_BONBON", "PYROCLASTIC_SCALE",
            "SCORCHED_CRAB_STICK", "SHARK_WATER_ORB", "SINGED_POWDER", "SPOOKY_WATER_ORB",
            "THUNDER_SHARDS", "TITANOBOA_SHED", "WATER_ORB", "WINTER_WATER_ORB");

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> items() throws IOException {
        Map<String, Map<String, Object>> byId = new HashMap<>();
        for (String file : FILES) {
            for (Map<String, Object> item : (List<Map<String, Object>>) YamlFileUtils.loadYaml(ITEMS.resolve(file).toFile()).get("items")) {
                byId.put(String.valueOf(item.get("id")), item);
            }
        }
        return byId;
    }

    @Test
    void everyFormerPlaceholderIsStillDefined() throws IOException {
        Map<String, Map<String, Object>> items = items();
        for (String id : FORMER_PLACEHOLDERS) assertTrue(items.containsKey(id), id);
        assertEquals(88, FORMER_PLACEHOLDERS.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void noItemCarriesThePlaceholderComponent() throws IOException {
        for (Map<String, Object> item : items().values()) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.getOrDefault("components", List.of())) {
                assertFalse("BAZAAR_PLACEHOLDER".equals(component.get("id")), String.valueOf(item.get("id")));
            }
        }
    }

    @Test
    void everyFormerPlaceholderHasASourceAndRealLore() throws IOException {
        Map<String, Map<String, Object>> items = items();
        for (String id : FORMER_PLACEHOLDERS) {
            Map<String, Object> item = items.get(id);
            assertTrue(item.get("source") instanceof String s && !s.isBlank(), id + " needs a source");
            assertFalse(String.valueOf(item.getOrDefault("lore", "")).contains("BAZAAR PLACEHOLDER"), id);
        }
    }
}
