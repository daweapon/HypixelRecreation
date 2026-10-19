package dev.daweapon.type.skyblockgeneric.firesale;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.taylor.TaylorSeasons;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FireSaleCatalogueTest {
    private static final File CONFIGURATION = new File("../configuration/skyblock");

    @Test
    void everyHistoricSaleSellsRealItemsAtAKnownTier() {
        Map<String, FireSale> sales = FireSaleCatalogue.parse(new File(CONFIGURATION, "firesales/fire_sales.yml"));
        assertEquals(120, sales.size());
        for (FireSale sale : sales.values()) {
            assertFalse(sale.items().isEmpty(), sale.id());
            assertTrue(List.of(350, 375, 650, 950, 1350, 1800).contains(sale.gems()), sale.id());
            sale.items().forEach(id -> assertNotNull(ItemType.get(id), sale.id() + " " + id));
            assertTrue(sale.unlimited() || sale.stock() > 0, sale.id());
        }
    }

    @Test
    void historyRunsFromTheFirstToTheLatestSale() {
        Map<String, FireSale> sales = FireSaleCatalogue.parse(new File(CONFIGURATION, "firesales/fire_sales.yml"));
        List<String> ids = List.copyOf(sales.keySet());
        assertEquals("2023-09-23", ids.getFirst());
        assertEquals("2026-10-10", ids.getLast());
    }

    @Test
    void everyTaylorShopEntryResolvesToAnItem() throws IOException {
        for (String shop : List.of("taylor_collection", "taylor_bundle_fall_2026", "taylor_bundle_summer_2026")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entries = (List<Map<String, Object>>) YamlFileUtils
                    .loadYaml(new File(CONFIGURATION, "shops/" + shop + ".yml")).get("entries");
            assertFalse(entries.isEmpty(), shop);
            for (Map<String, Object> entry : entries) {
                assertNotNull(ItemType.get(String.valueOf(entry.get("item"))), shop + " " + entry.get("item"));
            }
        }
    }

    @Test
    void taylorSeasonFollowsTheDateUnlessPinned() {
        File config = new File(CONFIGURATION, "taylor.yml");
        assertEquals("fall_2026", TaylorSeasons.current(config, LocalDate.of(2026, 10, 18)).id());
        assertEquals("summer_2026", TaylorSeasons.current(config, LocalDate.of(2026, 7, 1)).id());
        assertEquals("summer_2026", TaylorSeasons.current(config, LocalDate.of(2026, 1, 1)).id());
    }
}
