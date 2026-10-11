package net.swofty.type.skyblockgeneric.shop;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

public final class ShopStockLimits {
    private ShopStockLimits() {
    }

    public static boolean applies(SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        return type != null && BazaarCategories.getFromItem(type) != null;
    }
}
