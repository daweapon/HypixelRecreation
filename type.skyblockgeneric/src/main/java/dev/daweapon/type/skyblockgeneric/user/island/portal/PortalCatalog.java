package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollIslands;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PortalCatalog {
    public static final String HUB_ITEM_ID = "HUB_PORTAL";
    private static final File FILE = new File("./configuration/skyblock/island_portals.yml");
    private static Map<String, PortalDestination> destinations;

    private PortalCatalog() {
    }

    public static synchronized PortalDestination get(String itemId) {
        return all().get(itemId);
    }

    public static synchronized Map<String, PortalDestination> all() {
        if (destinations == null) destinations = load(FILE);
        return destinations;
    }

    public static Collection<PortalDestination> values() {
        return all().values();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, PortalDestination> load(File file) {
        try {
            Map<String, Map<String, Object>> raw = (Map<String, Map<String, Object>>) YamlFileUtils.loadYaml(file).get("portals");
            Map<String, PortalDestination> loaded = new LinkedHashMap<>();
            raw.forEach((itemId, entry) -> {
                String islandName = (String) entry.get("island");
                String scrollName = (String) entry.get("scroll");
                TravelScrollIslands island = islandName == null ? null : TravelScrollIslands.getFromInternalName(islandName);
                TravelScrollType scroll = scrollName == null ? null : TravelScrollType.getFromInternalName(scrollName);
                if (islandName != null && island == null) throw new IllegalStateException(itemId + " island " + islandName);
                if (scrollName != null && scroll == null) throw new IllegalStateException(itemId + " scroll " + scrollName);
                loaded.put(itemId, new PortalDestination(itemId, (String) entry.get("label"), island, scroll));
            });
            return loaded;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
