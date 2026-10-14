package net.swofty.type.crystalhollows.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.List;

public class GUIShopOdawa extends ShopView {
    public GUIShopOdawa() {
        super("Odawa", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        sell(ItemType.KALHUIKI_MASK, price(20, new ItemShopPrice(ItemType.OAK_PLANKS, 20)), false);
        sell(ItemType.JUNGLE_AMULET, price(500, new ItemShopPrice(ItemType.JUNGLE_HEART, 4)), false);
        sell(ItemType.TRIBAL_SPEAR, price(100, new ItemShopPrice(ItemType.JUNGLE_HEART, 1)), false);
        sell(ItemType.JUNGLE_PICKAXE, price(200), false);
        sell(ItemType.JUNGLE_KEY, price(100), true);
        sell(ItemType.ROUGH_AMETHYST_GEM, price(1), true);
    }

    private void sell(ItemType type, ShopPrice price, boolean stackable) {
        SkyBlockItem item = new SkyBlockItem(type);
        attachItem(stackable ? ShopItem.Stackable(item, 1, price) : ShopItem.Single(item, 1, price));
    }

    private static ShopPrice price(int sludgeJuice, ItemShopPrice... extra) {
        List<ShopPrice> elements = new java.util.ArrayList<>(List.of(new ItemShopPrice(ItemType.SLUDGE_JUICE, sludgeJuice)));
        elements.addAll(List.of(extra));
        return new CombinedShopPrice(elements);
    }
}
