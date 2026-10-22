package dev.daweapon.type.skyblockgeneric.farming.chum;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class MobyShopView extends CatalogueShopView {
    public static final String BOUGHT_BUCKET = "moby.bucket";

    public MobyShopView() {
        super("moby");
    }

    @Override
    protected ShopItem decorate(CatalogueEntry entry, ShopItem item, SkyBlockPlayer player) {
        if (entry.itemId().equals("EMPTY_CHUM_BUCKET")) {
            item.onPurchase(buyer -> ChapterTriggers.fire(buyer, BOUGHT_BUCKET));
        }
        return item;
    }
}
