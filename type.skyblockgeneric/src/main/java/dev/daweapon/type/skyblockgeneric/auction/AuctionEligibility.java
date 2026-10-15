package dev.daweapon.type.skyblockgeneric.auction;

import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.DefaultSoulboundComponent;

public final class AuctionEligibility {
    private AuctionEligibility() {}

    public static boolean canList(SkyBlockItem item) {
        return item != null && !item.isAir() && !item.isNA()
                && !item.hasComponent(DefaultSoulboundComponent.class)
                && (item.getItemType() == null || BazaarCategories.getFromItem(item.getItemType()) == null);
    }
}
