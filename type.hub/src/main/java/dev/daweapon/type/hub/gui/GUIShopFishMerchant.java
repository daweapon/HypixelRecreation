package dev.daweapon.type.hub.gui;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for the Fish Merchant NPC in the Hub, selling fish and a fishing rod.
public class GUIShopFishMerchant extends ShopView {
    public GUIShopFishMerchant() {
        super("Fish Merchant", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.FISHING_ROD), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.COD), 1, new CoinShopPrice(20)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.SALMON), 1, new CoinShopPrice(30)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.TROPICAL_FISH), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.PUFFERFISH), 1, new CoinShopPrice(40)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_NECKLACE), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_CLOAK), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_BELT), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_BRACELET), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_HELMET), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_CHESTPLATE), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_LEGGINGS), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ANGLER_BOOTS), 1, new CoinShopPrice(100)));
    }
}
