package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// The name a product is listed under on the Bazaar: the configured item name, or the id spelled out
// for vanilla items that carry no name of their own.
public final class BazaarNames {
    private static final Map<ItemType, String> CACHE = new ConcurrentHashMap<>();
    private static final Map<ItemType, String> LISTED_AS = Map.of(
            ItemType.IMPLOSION_SCROLL, "Implosion",
            ItemType.WITHER_SHIELD_SCROLL, "Wither Shield",
            ItemType.SHADOW_WARP_SCROLL, "Shadow Warp"
    );

    private BazaarNames() {
    }

    public static String of(ItemType type) {
        String listed = LISTED_AS.get(type);
        if (listed != null) {
            return listed;
        }
        String cached = CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(type.name());
        if (config == null) {
            return type.getDisplayName();
        }
        String name = config.getName();
        String resolved = name == null || name.isBlank() ? type.getDisplayName() : name.replaceAll("§.", "");
        CACHE.put(type, resolved);
        return resolved;
    }
}
