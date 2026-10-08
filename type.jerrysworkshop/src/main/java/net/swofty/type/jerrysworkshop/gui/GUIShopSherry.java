package net.swofty.type.jerrysworkshop.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

// Sherry's shop in Jerry's Workshop. The Festive Sinker is left out until its unlock is known.
public class GUIShopSherry extends ShopView {

    public GUIShopSherry() {
        super("Sherry", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(new ShopItem(new SkyBlockItem(ItemType.ICE_BAIT), 1, new CoinShopPrice(12), true, false));
        attachItem(new ShopItem(new SkyBlockItem(ItemType.SNOW_BLASTER), 1, new CoinShopPrice(75_000), false, false));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.ICE), 1, new CoinShopPrice(1)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.PACKED_ICE), 1, new CoinShopPrice(9)));
        attachItem(new ShopItem(new SkyBlockItem(ItemType.LARGE_WINTER_SACK), 1, new CoinShopPrice(250_000), false, false));
    }
}
