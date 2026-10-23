package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class TechnoshopView extends CatalogueShopView {
    public static final String CATALOGUE = "technoshop";

    public TechnoshopView() {
        super(CATALOGUE);
    }

    public static void open(SkyBlockPlayer player) {
        if (!TechnobladeShrine.technoshopOpen()) {
            player.sendMessage("<c>The Technoshop is closed!");
            return;
        }
        player.openView(new TechnoshopView());
    }

    @Override
    protected ShopItem decorate(CatalogueEntry entry, ShopItem item, SkyBlockPlayer player) {
        if (entry.itemId().equals("PIG_MASK")) {
            item.lockedWhen(viewer -> ProfileOnce.PIG_MASK.done(viewer) ? "<c>You can only buy one Pig Mask per profile!" : null);
            item.onPurchase(ProfileOnce.PIG_MASK::record);
        }
        return item;
    }
}
