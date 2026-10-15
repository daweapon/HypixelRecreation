package dev.daweapon.type.hub.gui;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for the Lumber Merchant NPC in the Hub, selling logs, axes, and basic wooden tools.
public class GUIShopLumberMerchant extends ShopView {
    public GUIShopLumberMerchant() {
        super("Lumber Merchant", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.OAK_LOG), 5, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BIRCH_LOG), 5, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.SPRUCE_LOG), 5, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.DARK_OAK_LOG), 5, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ACACIA_LOG), 5, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.JUNGLE_LOG), 5, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.STICK), 32, new CoinShopPrice(20)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ROOKIE_AXE), 1, new CoinShopPrice(12)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.PROMISING_AXE), 1, new CoinShopPrice(35)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PODZOL), 1, new CoinShopPrice(20)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SWEET_AXE), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.EFFICIENT_AXE), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WOODEN_SWORD), 1, new CoinShopPrice(5)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WOODEN_PICKAXE), 1, new CoinShopPrice(5)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WOODEN_SHOVEL), 1, new CoinShopPrice(5)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WOODEN_HOE), 1, new CoinShopPrice(5)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WOODEN_AXE), 1, new CoinShopPrice(5)));
    }
}
