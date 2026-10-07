package net.swofty.type.hub.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

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
