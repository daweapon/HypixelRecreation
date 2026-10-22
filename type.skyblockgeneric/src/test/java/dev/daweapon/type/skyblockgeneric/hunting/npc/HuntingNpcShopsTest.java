package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntingNpcShopsTest {
    private static final File SHOPS = new File("../configuration/skyblock/shops");

    @SuppressWarnings("unchecked")
    private static List<CatalogueEntry> entries(String shop) throws IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File(SHOPS, shop + ".yml"));
        return ((List<Map<String, Object>>) root.get("entries")).stream().map(CatalogueEntry::parse).toList();
    }

    @Test
    void albertSellsTheTwoPocketBlackHolesWithTheWikiCosts() throws IOException {
        List<CatalogueEntry> entries = entries("hunting_albert");
        assertEquals(2, entries.size());
        assertEquals(100_000, entries.get(0).cost().coins());
        assertEquals(Map.of("FIG_LOG", 64), entries.get(0).cost().items());
        assertEquals(1_000_000, entries.get(1).cost().coins());
        assertEquals(Map.of("ENCHANTED_MANGROVE_LOG", 64, "ENCHANTED_OBSIDIAN", 64, "SMALL_POCKET_BLACK_HOLE", 1), entries.get(1).cost().items());
        entries.forEach(entry -> assertTrue(entry.available(), entry.itemId()));
    }

    @Test
    void antSellsSixTierTwelveStonesForTwoMillionAndAThousandAndTwentyFourLogs() throws IOException {
        List<CatalogueEntry> entries = entries("hunting_ant");
        assertEquals(6, entries.size());
        for (CatalogueEntry entry : entries) {
            assertTrue(entry.itemId().startsWith("GENERATOR_UPGRADE_STONE_") && entry.itemId().endsWith("_12"), entry.itemId());
            assertEquals(2_000_000, entry.cost().coins());
            assertEquals(1, entry.cost().items().size());
            assertEquals(1024, entry.cost().items().values().iterator().next());
            assertTrue(entry.available(), entry.itemId());
            assertNull(entry.unavailableReason());
        }
    }

    @Test
    void maximSellsFiveUncommonBoosterUpgradesForOneOfEachFigstoneMangcoreAndHelixis() throws IOException {
        List<CatalogueEntry> entries = entries("hunting_maxim");
        assertEquals(5, entries.size());
        for (CatalogueEntry entry : entries) {
            assertEquals(Map.of("FIGSTONE", 1, "MANGCORE", 1, "HELIXIS", 1), entry.cost().items());
            assertEquals(0, entry.cost().coins());
            assertTrue(entry.available(), entry.itemId());
        }
    }

    @Test
    void confirmationStartsAtOneMillionCoins() {
        assertFalse(HuntingNpcCatalogueView.needsConfirmation(999_999));
        assertTrue(HuntingNpcCatalogueView.needsConfirmation(1_000_000));
        assertTrue(HuntingNpcCatalogueView.needsConfirmation(2_000_000));
    }
}
