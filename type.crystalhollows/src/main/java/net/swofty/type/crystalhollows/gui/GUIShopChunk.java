package net.swofty.type.crystalhollows.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIShopChunk extends ShopView {
    public GUIShopChunk() {
        super("Chunk", SINGLE_SLOT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SUPERBOOM_TNT), 1, new CoinShopPrice(2_500)));
    }
}
