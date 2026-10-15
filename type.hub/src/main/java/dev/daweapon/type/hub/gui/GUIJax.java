package dev.daweapon.type.hub.gui;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for Jax the Adventurer NPC in the Hub, selling combat drops and talismans.
public class GUIJax extends ShopView {

    public GUIJax() {
        super("Adventurer", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.ROTTEN_FLESH), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.BONE), 1, new CoinShopPrice(8)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.STRING), 1, new CoinShopPrice(10)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.SLIME_BALL), 1, new CoinShopPrice(14)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.GUNPOWDER), 1, new CoinShopPrice(10)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.ZOMBIE_TALISMAN), 1, new CoinShopPrice(500)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SKELETON_TALISMAN), 1, new CoinShopPrice(500)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.VILLAGE_AFFINITY_TALISMAN), 1, new CoinShopPrice(2500)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MINE_AFFINITY_TALISMAN), 1, new CoinShopPrice(2500)));
    }
}