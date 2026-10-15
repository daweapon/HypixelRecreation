package dev.daweapon.type.skyblockgeneric.shop;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

public final class ShopStockLimits {
    private ShopStockLimits() {
    }

    public static boolean applies(SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        return type != null && BazaarCategories.getFromItem(type) != null;
    }
}
