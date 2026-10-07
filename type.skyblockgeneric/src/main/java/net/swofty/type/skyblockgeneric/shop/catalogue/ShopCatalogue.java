package net.swofty.type.skyblockgeneric.shop.catalogue;

import net.swofty.commons.YamlFileUtils;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import org.tinylog.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ShopCatalogue(String id, String title, int[] slots, List<CatalogueEntry> entries) {
    private static final File SHOPS_DIRECTORY = new File("./configuration/skyblock/shops");
    private static final Map<String, ShopCatalogue> CACHE = new ConcurrentHashMap<>();

    public static ShopCatalogue get(String id) {
        return CACHE.computeIfAbsent(id, ShopCatalogue::load);
    }

    @SuppressWarnings("unchecked")
    private static ShopCatalogue load(String id) {
        try {
            Map<String, Object> root = YamlFileUtils.loadYaml(new File(SHOPS_DIRECTORY, id + ".yml"));
            List<Map<String, Object>> rawEntries = (List<Map<String, Object>>) root.getOrDefault("entries", Collections.emptyList());
            return new ShopCatalogue(
                    id,
                    String.valueOf(root.getOrDefault("title", id)),
                    slots(root.get("slots")),
                    rawEntries.stream().map(CatalogueEntry::parse).toList()
            );
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load shop catalogue {}", id);
            return new ShopCatalogue(id, id, ShopView.DEFAULT, List.of());
        }
    }

    private static int[] slots(Object raw) {
        if (raw instanceof List<?> list) {
            return list.stream().mapToInt(value -> ((Number) value).intValue()).toArray();
        }
        if ("SINGLE_SLOT".equals(raw)) return ShopView.SINGLE_SLOT;
        return ShopView.DEFAULT;
    }
}
