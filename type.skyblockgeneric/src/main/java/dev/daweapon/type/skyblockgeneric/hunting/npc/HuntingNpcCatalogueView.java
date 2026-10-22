package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public class HuntingNpcCatalogueView extends CatalogueShopView {
    public static final double CONFIRM_FROM_COINS = 1_000_000;

    private final @Nullable String purchaseTrigger;

    public HuntingNpcCatalogueView(String catalogueId, @Nullable String purchaseTrigger) {
        super(catalogueId);
        this.purchaseTrigger = purchaseTrigger;
    }

    public static boolean needsConfirmation(double coins) {
        return coins >= CONFIRM_FROM_COINS;
    }

    @Override
    protected ShopItem decorate(CatalogueEntry entry, ShopItem item, SkyBlockPlayer player) {
        if (needsConfirmation(entry.cost().coins())) item.withConfirmation();
        if (purchaseTrigger != null) {
            java.util.function.Consumer<SkyBlockPlayer> previous = item.getOnPurchase();
            item.onPurchase(buyer -> {
                if (previous != null) previous.accept(buyer);
                ChapterTriggers.fire(buyer, purchaseTrigger);
            });
        }
        return item;
    }
}
