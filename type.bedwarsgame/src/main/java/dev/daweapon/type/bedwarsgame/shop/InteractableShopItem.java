package dev.daweapon.type.bedwarsgame.shop;

import dev.daweapon.type.bedwarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;

// Adapts a SimpleInteractableItem so it can be sold as a regular shop item.
public class InteractableShopItem extends ShopItem {
    private final SimpleInteractableItem item;

    public InteractableShopItem(SimpleInteractableItem item) {
        super(
            item.getId(),
            item.getShopData().name(),
            item.getShopData().description(),
            item.getShopData().price(),
            item.getShopData().amount(),
            item.getShopData().currency(),
            item.getBlandItem().material(),
            item.getShopData().hotbarItemType()
        );
        this.item = item;
    }

    @Override
    public void onPurchase(BedWarsPlayer player) {
        giveItem(player, item.getItemStack().withAmount(getAmount()));
    }
}
