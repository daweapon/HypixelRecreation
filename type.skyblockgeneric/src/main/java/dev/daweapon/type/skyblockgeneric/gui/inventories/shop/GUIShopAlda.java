package dev.daweapon.type.skyblockgeneric.gui.inventories.shop;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopAlda extends ShopView {
    public GUIShopAlda() {
        super(Text.key("gui_shop.alda.title"), SINGLE_SLOT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ABIPHONE_BASIC), 1, new CoinShopPrice(100000)));
    }
}
