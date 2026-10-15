package dev.daweapon.type.skyblockgeneric.bazaar;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;

import java.util.Locale;

// Derives the list of attribute shard ItemTypes tradeable on the bazaar from the attribute registry.
public final class AttributeBazaarItems {
    private AttributeBazaarItems() {
    }

    public static ItemType[] values() {
        return AttributeRegistry.values().stream().map(definition -> ItemType.valueOf("SHARD_"
                        + definition.shard().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_")
                        .replaceAll("^_|_$", "")))
                .toArray(ItemType[]::new);
    }
}
