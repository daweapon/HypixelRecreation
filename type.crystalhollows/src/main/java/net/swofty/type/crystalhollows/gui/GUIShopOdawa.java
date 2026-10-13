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
        sell(ItemType.JUNGLE_KEY, 100);
        sell(ItemType.KALHUIKI_MASK, price(20, new ItemShopPrice(ItemType.JUNGLE_LOG, 20)));
        sell(ItemType.JUNGLE_PICKAXE, 200);
        sell(ItemType.TRIBAL_SPEAR, price(100, new ItemShopPrice(ItemType.JUNGLE_HEART, 1)));
        sell(ItemType.JUNGLE_AMULET, price(500, new ItemShopPrice(ItemType.JUNGLE_HEART, 4)));
    }

    private void sell(ItemType type, int sludgeJuice) {
        sell(type, price(sludgeJuice));
    }

    private void sell(ItemType type, ShopPrice price) {
        attachItem(ShopItem.Single(new SkyBlockItem(type), 1, price));
    }

    private static ShopPrice price(int sludgeJuice, ItemShopPrice... extra) {
        List<ShopPrice> elements = new java.util.ArrayList<>(List.of(new ItemShopPrice(ItemType.SLUDGE_JUICE, sludgeJuice)));
        elements.addAll(List.of(extra));
        return new CombinedShopPrice(elements);
    }
}
