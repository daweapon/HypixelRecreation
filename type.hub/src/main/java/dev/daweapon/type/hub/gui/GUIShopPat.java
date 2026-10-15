package dev.daweapon.type.hub.gui;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for the Pat NPC in the Hub, selling flint and gravel.
public class GUIShopPat extends ShopView {
    public GUIShopPat() {
        super("Pat", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.FLINT), 10, new CoinShopPrice(60)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GRAVEL), 15, new CoinShopPrice(65)));
    }
}
