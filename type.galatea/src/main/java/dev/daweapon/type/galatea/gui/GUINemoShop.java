package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUINemoShop extends CatalogueShopView {

    public GUINemoShop() {
        super("nemo");
    }

    @Override
    protected ShopItem decorate(CatalogueEntry entry, ShopItem item, SkyBlockPlayer player) {
        item.withConfirmation();
        return item;
    }
}
