package dev.daweapon.type.skyblockgeneric.collection;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gems.Gemstone;

public final class CollectionNames {
    public static final ItemType GEMSTONE_KEY = ItemType.FINE_JASPER_GEM;

    private CollectionNames() {
    }

    public static ItemType canonical(ItemType type) {
        return type != null && type != GEMSTONE_KEY && Gemstone.getFromItemType(type) != null ? GEMSTONE_KEY : type;
    }

    public static String displayName(ItemType type) {
        return type == GEMSTONE_KEY ? "Gemstone" : type.getDisplayName();
    }
}
