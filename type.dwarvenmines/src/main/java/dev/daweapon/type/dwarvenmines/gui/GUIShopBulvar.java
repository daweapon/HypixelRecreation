package dev.daweapon.type.dwarvenmines.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.MinionShopPrice;

import java.util.List;

public class GUIShopBulvar extends ShopView {
    private static final int TIER = 12;
    private static final int COINS = 2_000_000;

    public GUIShopBulvar() {
        super("Bulvar", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        sell("COBBLESTONE_MINION", "ENCHANTED_COBBLESTONE", 1024);
        sell("OBSIDIAN_MINION", "ENCHANTED_OBSIDIAN", 1024);
        sell("COAL_MINION", "ENCHANTED_COAL_BLOCK", 16);
        sell("IRON_MINION", "ENCHANTED_IRON_BLOCK", 16);
        sell("GOLD_MINION", "ENCHANTED_GOLD_BLOCK", 16);
        sell("DIAMOND_MINION", "ENCHANTED_DIAMOND_BLOCK", 16);
        sell("LAPIS_MINION", "ENCHANTED_LAPIS_LAZULI_BLOCK", 64);
        sell("EMERALD_MINION", "ENCHANTED_EMERALD_BLOCK", 16);
        sell("REDSTONE_MINION", "ENCHANTED_REDSTONE_BLOCK", 32);
        sell("MITHRIL_MINION", "REFINED_MITHRIL", 16);
        sell("HARD_STONE_MINION", "CONCENTRATED_STONE", 16);
    }

    private void sell(String minionId, String materialId, int amount) {
        ItemType minion = ItemType.get(minionId);
        ItemType material = ItemType.get(materialId);
        if (minion == null || material == null) return;
        SkyBlockItem item = new SkyBlockItem(minion);
        item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(TIER, 0));
        attachItem(new ShopItem(item, 1, new CombinedShopPrice(List.of(
                new MinionShopPrice(minion, TIER - 1), new ItemShopPrice(material, amount), new CoinShopPrice(COINS))),
                false, false).withConfirmation());
    }
}
