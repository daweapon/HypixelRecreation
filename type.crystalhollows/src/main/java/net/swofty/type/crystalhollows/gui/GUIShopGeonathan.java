package net.swofty.type.crystalhollows.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopGeonathan extends ShopView {
    public static final long BIOFUEL_PRICE = 20_000;

    public GUIShopGeonathan() {
        super("Geonathan Greatforge", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BIOFUEL), 1, new CoinShopPrice(BIOFUEL_PRICE)));
    }
}
