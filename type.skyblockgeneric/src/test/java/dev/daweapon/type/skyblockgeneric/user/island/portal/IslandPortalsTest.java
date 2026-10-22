package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandPortalsTest {
    private static final Path CATALOGUE = Path.of("../configuration/skyblock/items/catalogue/portals_sacks.yml");

    @Test
    void thirtyEightPortalsAreConfigured() {
        assertEquals(38, PortalCatalog.all().size());
        assertNotNull(PortalCatalog.get(PortalCatalog.HUB_ITEM_ID));
    }

    @SuppressWarnings("unchecked")
    @Test
    void everyPortalItemIsPlaceableAndConfigured() throws Exception {
        List<Map<String, Object>> items = (List<Map<String, Object>>) YamlFileUtils.loadYaml(CATALOGUE.toFile()).get("items");
        Set<String> placeable = items.stream()
                .filter(item -> ((String) item.get("id")).endsWith("_PORTAL"))
                .peek(item -> {
                    assertNull(item.get("unavailable"), item.get("id") + " should be placeable");
                    List<Map<String, Object>> components = (List<Map<String, Object>>) item.get("components");
                    assertTrue(components.stream().anyMatch(c -> "PLACE_EVENT".equals(c.get("id"))
                            && IslandPortalPlacement.HANDLER_ID.equals(c.get("handler_id"))), item.get("id") + " has no place handler");
                })
                .map(item -> (String) item.get("id"))
                .collect(Collectors.toSet());
        assertEquals(PortalCatalog.all().keySet(), placeable);
    }

    @Test
    void portalFacesTheBlockTheClickersLooksFrom() {
        assertEquals(0, PortalShape.frontFromYaw(0));
        assertEquals(1, PortalShape.frontFromYaw(90));
        assertEquals(2, PortalShape.frontFromYaw(180));
        assertEquals(3, PortalShape.frontFromYaw(-90));
    }

    @Test
    void legacyHubPortalKeepsItsOriginalFootprint() {
        PlacedPortal portal = IslandPortals.LEGACY_HUB_PORTAL;
        assertArrayEquals(new int[]{9, 42}, PortalShape.world(portal, 3, 1));
        assertArrayEquals(new int[]{3, 44}, PortalShape.world(portal, -3, -1));
        assertTrue(PortalShape.isPartOf(portal, 6, 100, 43));
        assertTrue(PortalShape.isPortalPlane(portal, 6, 102, 43));
    }

    @Test
    void rotatedPortalsSwapAxes() {
        PlacedPortal east = new PlacedPortal("HUB_PORTAL", 0, 0, 0, 1);
        assertArrayEquals(new int[]{1, 3}, PortalShape.world(east, 3, 1));
    }

    @Test
    void portalsRoundTripThroughTheDatabaseString() {
        IslandPortals loaded = IslandPortals.deserialize("HUB_PORTAL,1,2,3,0;MINING_1_PORTAL,4,5,6,2");
        assertEquals("HUB_PORTAL,1,2,3,0;MINING_1_PORTAL,4,5,6,2", loaded.serialize());
        assertEquals(2, loaded.all().size());
    }

    @Test
    void islandsWithoutSavedPortalsGetTheStarterHubPortal() {
        assertEquals(List.of(IslandPortals.LEGACY_HUB_PORTAL), IslandPortals.deserialize(null).all());
        assertEquals(0, IslandPortals.deserialize("").all().size());
    }
}
