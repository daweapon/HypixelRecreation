package dev.daweapon.type.hub.gui;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for the Weaponsmith NPC in the Hub, selling swords, bows, and arrows.
public class GUIShopWeaponsmith extends ShopView {

    public GUIShopWeaponsmith() {
        super("Weaponsmith", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.UNDEAD_SWORD), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.END_SWORD), 1, new CoinShopPrice(150)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SPIDER_SWORD), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(Material.DIAMOND_SWORD), 1, new CoinShopPrice(60)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ROGUE_SWORD), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(new SkyBlockItem(Material.BOW), 1, new CoinShopPrice(25)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.FLINT_ARROW), 1, new CoinShopPrice(5)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WITHER_BOW), 1, new CoinShopPrice(250)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ARTISANAL_SHORTBOW), 1, new CoinShopPrice(600)));
    }
}
