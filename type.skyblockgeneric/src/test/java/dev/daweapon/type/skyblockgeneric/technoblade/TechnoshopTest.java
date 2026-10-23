package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnoshopTest {
    @SuppressWarnings("unchecked")
    private static Map<String, CatalogueEntry> entries() throws java.io.IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File("../configuration/skyblock/shops/technoshop.yml"));
        Map<String, CatalogueEntry> byItem = new java.util.LinkedHashMap<>();
        for (Map<String, Object> raw : (List<Map<String, Object>>) root.get("entries")) {
            CatalogueEntry entry = CatalogueEntry.parse(raw);
            byItem.put(entry.itemId(), entry);
        }
        return byItem;
    }

    @Test
    void sellsExactlyTheThreeNonShinyItems() throws Exception {
        assertEquals(List.of("PIG_MASK", "THE_ART_OF_WAR", "BLOOD_GOD_CREST"), List.copyOf(entries().keySet()));
    }

    @Test
    void pigMaskCostsTenThousandCoinsAndTwoEnchantedPork() throws Exception {
        CatalogueEntry mask = entries().get("PIG_MASK");
        assertEquals(10_000, mask.cost().coins());
        assertEquals(Map.of("ENCHANTED_PORK", 2), mask.cost().items());
    }

    @Test
    void artOfWarCostsFiveMillionCoinsAndTwoGrilledPork() throws Exception {
        CatalogueEntry art = entries().get("THE_ART_OF_WAR");
        assertEquals(5_000_000, art.cost().coins());
        assertEquals(Map.of("ENCHANTED_GRILLED_PORK", 2), art.cost().items());
    }

    @Test
    void bloodGodCrestCostsHalfAMillionCoinsAndThirtyTwoEnchantedPork() throws Exception {
        CatalogueEntry crest = entries().get("BLOOD_GOD_CREST");
        assertEquals(500_000, crest.cost().coins());
        assertEquals(Map.of("ENCHANTED_PORK", 32), crest.cost().items());
    }

    @Test
    void everyEntryHasAPositivePrice() throws Exception {
        entries().values().forEach(entry -> assertTrue(entry.cost().coins() > 0));
    }
}
