package net.swofty.type.jerrysworkshop.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

// Frosty's shop in Jerry's Workshop, selling the Snow Cannon used to defend the gift piles.
public class GUIShopFrosty extends ShopView {

    public GUIShopFrosty() {
        super("Frosty", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SNOW_CANNON), 1, new CoinShopPrice(1000)));
    }
}
