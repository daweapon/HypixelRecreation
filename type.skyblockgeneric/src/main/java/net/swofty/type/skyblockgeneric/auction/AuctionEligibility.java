package net.swofty.type.skyblockgeneric.auction;

import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DefaultSoulboundComponent;

public final class AuctionEligibility {
    private AuctionEligibility() {}

    public static boolean canList(SkyBlockItem item) {
        return item != null && !item.isAir() && !item.isNA()
                && !item.hasComponent(DefaultSoulboundComponent.class)
                && (item.getItemType() == null || BazaarCategories.getFromItem(item.getItemType()) == null);
    }
}
