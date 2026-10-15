package dev.daweapon.type.dwarvenmines.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for Lumina, the Dwarven Mines NPC who sells torches and lanterns.
public class GUIShopLumina extends ShopView {
    public GUIShopLumina() {
        super("Lumina", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.TORCH), 64, new CoinShopPrice(32)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.LANTERN), 1, new CoinShopPrice(10_000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.INFINI_TORCH), 1, new CoinShopPrice(100_000)));
    }
}
