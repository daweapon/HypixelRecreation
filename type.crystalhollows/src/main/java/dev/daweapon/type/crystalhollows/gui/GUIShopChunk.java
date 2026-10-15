package dev.daweapon.type.crystalhollows.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopChunk extends ShopView {
    public GUIShopChunk() {
        super("Chunk", SINGLE_SLOT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SUPERBOOM_TNT), 1, new CoinShopPrice(2_500)));
    }
}
