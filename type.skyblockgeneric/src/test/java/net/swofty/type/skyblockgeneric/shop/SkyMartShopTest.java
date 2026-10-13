package net.swofty.type.skyblockgeneric.shop;

import net.swofty.commons.YamlFileUtils;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyMartShopTest {
    private static final List<String> SHOPS = List.of(
            "skymart_farming_essentials", "skymart_farming_tools", "skymart_barn_skins",
            "skymart_greenhouse_skins", "skymart_pests");

    @SuppressWarnings("unchecked")
    private static List<CatalogueEntry> entries(String shop) {
        try {
            return load(shop);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<CatalogueEntry> load(String shop) throws java.io.IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File("../configuration/skyblock/shops/" + shop + ".yml"));
        return ((List<Map<String, Object>>) root.get("entries")).stream().map(CatalogueEntry::parse).toList();
    }

    @Test
    void everyShopLoadsWithASlotPerEntry() {
        int total = 0;
        for (String shop : SHOPS) {
            List<CatalogueEntry> entries = entries(shop);
            assertFalse(entries.isEmpty(), shop);
            total += entries.size();
        }
        assertEquals(70, total);
    }

    @Test
    void biofuelAndToolPricesMatchTheVideoAndWiki() {
        CatalogueEntry biofuel = entries("skymart_farming_essentials").getFirst();
        assertEquals("BIOFUEL", biofuel.itemId());
        assertEquals(5_000, biofuel.cost().coins());
        for (CatalogueEntry tool : entries("skymart_farming_tools").subList(0, 12)) {
            assertEquals(250, tool.cost().copper(), tool.itemId());
        }
    }

    @Test
    void enchantedBooksNameARealEnchantment() {
        long books = entries("skymart_farming_essentials").stream().filter(entry -> entry.enchantment() != null).count();
        assertEquals(2, books);
        entries("skymart_farming_essentials").stream().filter(entry -> entry.enchantment() != null)
                .forEach(entry -> assertNotNull(EnchantmentType.valueOf(entry.enchantment())));
    }

    @Test
    void everythingSoldExistsExceptTheUnbuiltMinion() {
        for (String shop : SHOPS) {
            for (CatalogueEntry entry : entries(shop)) {
                if (entry.itemId().equals("SUNFLOWER_GENERATOR_12")) continue;
                assertNotNull(entry.itemType(), shop + " " + entry.itemId());
                assertTrue(entry.cost().purchasable(), shop + " " + entry.itemId());
            }
        }
    }
}
