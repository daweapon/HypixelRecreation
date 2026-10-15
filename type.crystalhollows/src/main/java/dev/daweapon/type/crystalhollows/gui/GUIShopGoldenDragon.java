package dev.daweapon.type.crystalhollows.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.List;

public class GUIShopGoldenDragon extends ShopView {
    public static final long EGG_COINS = 500_000_000L;
    public static final int EGG_GOLD_BLOCKS = 50;
    public static final List<ItemType> EGG_GEMS = List.of(
            ItemType.PERFECT_AMETHYST_GEM, ItemType.PERFECT_JADE_GEM, ItemType.PERFECT_SAPPHIRE_GEM,
            ItemType.PERFECT_AMBER_GEM, ItemType.PERFECT_TOPAZ_GEM);

    public GUIShopGoldenDragon() {
        super("Golden Dragon", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        SkyBlockItem egg = new SkyBlockItem(ItemType.GOLDEN_DRAGON_PET);
        egg.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        attachItem(ShopItem.Single(egg, 1, eggPrice())
                .onPurchase(player -> player.getAchievementHandler().completeAchievement("skyblock.the_golden_dragon_s_egg")));
    }

    public static ShopPrice eggPrice() {
        List<ShopPrice> elements = new java.util.ArrayList<>();
        elements.add(new CoinShopPrice(EGG_COINS));
        elements.add(new ItemShopPrice(ItemType.ENCHANTED_GOLD_BLOCK, EGG_GOLD_BLOCKS));
        EGG_GEMS.forEach(gem -> elements.add(new ItemShopPrice(gem, 1)));
        return new CombinedShopPrice(elements);
    }
}
