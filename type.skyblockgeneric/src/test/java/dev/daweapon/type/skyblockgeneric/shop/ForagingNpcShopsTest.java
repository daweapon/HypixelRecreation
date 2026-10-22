package dev.daweapon.type.skyblockgeneric.shop;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForagingNpcShopsTest {
    private static final File SHOPS = new File("../configuration/skyblock/shops");

    @SuppressWarnings("unchecked")
    private static List<CatalogueEntry> entries(String shop) throws IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File(SHOPS, shop + ".yml"));
        return ((List<Map<String, Object>>) root.get("entries")).stream().map(CatalogueEntry::parse).toList();
    }

    @Test
    void sawyerSellsTheForagingFortuneBoosterForFiftyThousand() throws IOException {
        List<CatalogueEntry> entries = entries("sawyer");
        assertEquals(1, entries.size());
        assertEquals("FORAGING_FORTUNE_BOOSTER", entries.getFirst().itemId());
        assertEquals(50_000, entries.getFirst().cost().coins());
        assertTrue(entries.getFirst().available());
    }

    @Test
    void alanSellsBothHuntaxesAndTheUpgradeNeedsTheFirst() throws IOException {
        List<CatalogueEntry> entries = entries("alan");
        assertEquals(25_000, entries.get(0).cost().coins());
        assertEquals(300_000, entries.get(1).cost().coins());
        assertEquals(Map.of("VENATOR_GENESIS", 1), entries.get(1).cost().items());
        entries.forEach(entry -> assertTrue(entry.available(), entry.itemId()));
    }

    @Test
    void amauryPricesMatchTheWikiShop() throws IOException {
        List<CatalogueEntry> entries = entries("amaury");
        assertEquals(List.of(5_000D, 1_000_000D, 120_000D, 140_000D, 130_000D, 110_000D),
                entries.stream().map(entry -> entry.cost().coins()).toList());
        assertEquals(Map.of("SERIOUSLY_DAMAGED_AXE", 1), entries.get(1).cost().items());
        entries.forEach(entry -> assertTrue(entry.available(), entry.itemId()));
    }

    @Test
    void nemoSellsSnorkelingArmorAndTheRespirationBook() throws IOException {
        List<CatalogueEntry> entries = entries("nemo");
        assertEquals(5, entries.size());
        for (int index = 0; index < 4; index++) assertEquals(50_000, entries.get(index).cost().coins());
        CatalogueEntry book = entries.get(4);
        assertEquals("RESPIRATION", book.enchantment());
        assertEquals(4, book.enchantmentLevel());
        assertEquals(4_000_000, book.cost().coins());
        assertEquals(Map.of("SEA_LUMIES", 32), book.cost().items());
        entries.forEach(entry -> assertTrue(entry.available(), entry.itemId()));
    }
}
