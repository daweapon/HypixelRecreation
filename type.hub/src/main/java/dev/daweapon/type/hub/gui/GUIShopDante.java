package dev.daweapon.type.hub.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.dante.DanteAccess;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopDante extends ShopView {
    public GUIShopDante() {
        super("Dante", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        sell(ItemType.HAPPY_MASK, DanteAccess.HAPPY_MASK_PRICE);
        sell(ItemType.DANTE_TALISMAN, DanteAccess.TALISMAN_PRICE);
    }

    private void sell(ItemType item, long coins) {
        attachItem(ShopItem.Single(new SkyBlockItem(item), 1, new CoinShopPrice(coins)));
    }
}
