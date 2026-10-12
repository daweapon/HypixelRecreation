package net.swofty.type.skyblockgeneric.item.catalogue;

import net.swofty.commons.YamlFileUtils;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;
import net.swofty.type.skyblockgeneric.warps.TravelScrollType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalsSacksCatalogueTest {
    private static final Path FILE = Path.of("../configuration/skyblock/items/catalogue/portals_sacks.yml");

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items() throws Exception {
        return (List<Map<String, Object>>) YamlFileUtils.loadYaml(FILE.toFile()).get("items");
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyTravelScrollPointsAtARealWarp() throws Exception {
        for (Map<String, Object> item : items()) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.get("components")) {
                if (!"TRAVEL_SCROLL".equals(component.get("id"))) continue;
                if (component.get("island") instanceof String island) {
                    assertNotNull(TravelScrollIslands.getFromInternalName(island), item.get("id") + " island " + island);
                } else {
                    TravelScrollType.valueOf((String) component.get("scroll_type"));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void sacksStoreSomethingWithACapacity() throws Exception {
        for (Map<String, Object> item : items()) {
            for (Map<String, Object> component : (List<Map<String, Object>>) item.get("components")) {
                if (!"SACK".equals(component.get("id"))) continue;
                assertTrue(((List<?>) component.get("valid_items")).size() > 0, item.get("id") + " has no items");
                assertTrue(((Number) component.get("max_capacity")).intValue() > 0, item.get("id") + " has no capacity");
            }
        }
    }
}
