package net.swofty.type.dwarvenmines.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.ArrayList;
import java.util.List;

public class GUIShopBubu extends ShopView {
    public GUIShopBubu() {
        super("Bubu", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        sell(ItemType.FRACTURED_MITHRIL_PICKAXE, new CoinShopPrice(10_000));
        sell(ItemType.BANDAGED_MITHRIL_PICKAXE, price(0, item(ItemType.FRACTURED_MITHRIL_PICKAXE, 1), item(ItemType.ENCHANTED_MITHRIL, 2)));
        sell(ItemType.MITHRIL_PICKAXE, price(0, item(ItemType.BANDAGED_MITHRIL_PICKAXE, 1), item(ItemType.ENCHANTED_MITHRIL, 64),
                item(ItemType.BEJEWELED_HANDLE, 1), item(ItemType.ENCHANTED_GOLD_INGOT, 32)));
        sell(ItemType.REFINED_MITHRIL_PICKAXE, price(0, item(ItemType.MITHRIL_PICKAXE, 1), item(ItemType.REFINED_MITHRIL, 2)));
        sell(ItemType.RUSTY_TITANIUM_PICKAXE, new CoinShopPrice(50_000));
        sell(ItemType.TITANIUM_PICKAXE, price(0, item(ItemType.RUSTY_TITANIUM_PICKAXE, 1), item(ItemType.ENCHANTED_TITANIUM, 4),
                item(ItemType.BEJEWELED_HANDLE, 1)));
        sell(ItemType.REFINED_TITANIUM_PICKAXE, price(0, item(ItemType.TITANIUM_PICKAXE, 1), item(ItemType.REFINED_TITANIUM, 1)));
        sell(ItemType.BIOFUEL, new CoinShopPrice(20_000));
    }

    private void sell(ItemType type, ShopPrice price) {
        attachItem(ShopItem.Single(new SkyBlockItem(type), 1, price));
    }

    private static ItemShopPrice item(ItemType type, int amount) {
        return new ItemShopPrice(type, amount);
    }

    private static ShopPrice price(long coins, ItemShopPrice... items) {
        List<ShopPrice> elements = new ArrayList<>(List.of(items));
        if (coins > 0) elements.add(new CoinShopPrice(coins));
        return new CombinedShopPrice(elements);
    }
}
