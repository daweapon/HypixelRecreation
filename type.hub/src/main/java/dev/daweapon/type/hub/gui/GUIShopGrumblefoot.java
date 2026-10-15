package dev.daweapon.type.hub.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopGrumblefoot extends ShopView {
    public GUIShopGrumblefoot() {
        super(Text.of("Grumblefoot"), SINGLE_SLOT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(new ShopItem(new SkyBlockItem(ItemType.HUB_WIZARD_TOWER_TRAVEL_SCROLL), 1,
                new CoinShopPrice(11_111), false, false));
    }
}
