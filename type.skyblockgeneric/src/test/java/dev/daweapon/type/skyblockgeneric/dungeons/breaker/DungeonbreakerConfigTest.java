package dev.daweapon.type.skyblockgeneric.dungeons.breaker;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatacombsFloorRequirement;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.ShopRequirement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonbreakerConfigTest {
    private static final String CONFIG = "../configuration/skyblock/";

    @SuppressWarnings("unchecked")
    private static Map<String, Object> catalogueItem(String file, String id) throws Exception {
        List<Map<String, Object>> items = (List<Map<String, Object>>) YamlFileUtils.loadYaml(new File(CONFIG + "items/catalogue/" + file)).get("items");
        return items.stream().filter(item -> id.equals(item.get("id"))).findFirst().orElseThrow();
    }

    @Test
    @SuppressWarnings("unchecked")
    void opheliaSellsItForFiveHundredWitherEssenceOnceFloorThreeIsDone() throws Exception {
        Map<String, Object> shop = YamlFileUtils.loadYaml(new File(CONFIG + "shops/ophelia.yml"));
        CatalogueEntry entry = ((List<Map<String, Object>>) shop.get("entries")).stream()
                .map(CatalogueEntry::parse).filter(each -> each.itemId().equals("DUNGEONBREAKER")).findFirst().orElseThrow();
        assertNull(entry.unavailableReason());
        assertEquals("WITHER", entry.cost().essenceType());
        assertEquals(500, entry.cost().essence());
        CatacombsFloorRequirement requirement = assertInstanceOf(CatacombsFloorRequirement.class, entry.requirement());
        assertEquals(CatacombsFloor.FLOOR_THREE, requirement.floor());
        assertEquals("<c>Requires completing The Catacombs Floor III!", requirement.lockLine());
    }

    @Test
    void floorRequirementsParseAndFallBackToReputation() {
        assertInstanceOf(CatacombsFloorRequirement.class, ShopRequirement.parse(Map.of("catacombs_floor", "floor_seven")));
        assertNull(ShopRequirement.parse(Map.of("catacombs_floor", "FLOOR_NINE")));
        assertNull(ShopRequirement.parse(null));
        assertFalse(ShopRequirement.parse(Map.of("faction", "MAGE", "tier", "HERO")) instanceof CatacombsFloorRequirement);
    }

    @Test
    @SuppressWarnings("unchecked")
    void theItemIsRealWithLiveChargesInItsLore() throws Exception {
        Map<String, Object> item = catalogueItem("misc_b.yml", "DUNGEONBREAKER");
        assertNull(item.get("unavailable"));
        List<String> lore = (List<String>) item.get("lore");
        assertTrue(lore.stream().noneMatch(line -> line.contains("Charges:")), "charges come from the lore handler");
        List<Map<String, Object>> components = (List<Map<String, Object>>) item.get("components");
        assertTrue(components.stream().anyMatch(component -> "LORE_UPDATE".equals(component.get("id"))
                && Dungeonbreaker.LORE_HANDLER.equals(component.get("handler_id"))));
    }

    @Test
    void echoesOfTheLostIsBuyableWithHypixelsValuesAndCosts() throws Exception {
        JSONObject root = new JSONObject(Files.readString(Path.of(CONFIG + "essence_shops.json")));
        JSONArray perks = root.getJSONObject("WITHER").getJSONArray("perks");
        JSONObject echoes = null;
        for (int i = 0; i < perks.length(); i++) {
            if (Dungeonbreaker.ECHOES_OF_THE_LOST.equals(perks.getJSONObject(i).getString("id"))) echoes = perks.getJSONObject(i);
        }
        assertTrue(echoes != null && echoes.isNull("unavailable"));
        assertEquals("Echoes of the Lost", echoes.getString("name"));
        assertEquals(List.of(1, 2, 3, 4, 5), echoes.getJSONArray("values").toList());
        assertEquals(List.of(250, 500, 1000, 2000, 5000), echoes.getJSONArray("costs").toList());
    }
}
