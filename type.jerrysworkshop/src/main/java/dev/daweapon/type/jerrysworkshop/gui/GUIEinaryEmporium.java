package dev.daweapon.type.jerrysworkshop.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.MinionShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.NorthStarShopPrice;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

// Einary's Emporium, priced in North Stars and open all year. Only the Gift Talismans are limited
// to real-life December, as on Hypixel.
public class GUIEinaryEmporium extends ShopView {
    private static final String DECEMBER_ONLY = "<c>Only sold in December!";

    public GUIEinaryEmporium() {
        super("Einary's Emporium", DEFAULT);
    }

    public static boolean isDecember() {
        return LocalDate.now().getMonth() == Month.DECEMBER;
    }

    @Override
    public void initializeShopItems() {
        talisman(ItemType.WHITE_GIFT_TALISMAN, new NorthStarShopPrice(20));
        talisman(ItemType.GREEN_GIFT_TALISMAN, price(40, new ItemShopPrice(ItemType.WHITE_GIFT_TALISMAN, 1)));
        talisman(ItemType.BLUE_GIFT_TALISMAN, price(60, new ItemShopPrice(ItemType.GREEN_GIFT_TALISMAN, 1)));
        talisman(ItemType.PURPLE_GIFT_TALISMAN, price(100, new ItemShopPrice(ItemType.BLUE_GIFT_TALISMAN, 1)));
        talisman(ItemType.GOLD_GIFT_TALISMAN, price(200, new ItemShopPrice(ItemType.PURPLE_GIFT_TALISMAN, 1),
                new ItemShopPrice(ItemType.GOLD_GIFT, 1)));

        sell(new SkyBlockItem(ItemType.REINDEER_PET), 1, new NorthStarShopPrice(60));
        sell(new SkyBlockItem(ItemType.SNOW_CLOAK), 1, new NorthStarShopPrice(10));
        sell(new SkyBlockItem(ItemType.SNOW_BELT), 1, new NorthStarShopPrice(10));
        sell(new SkyBlockItem(ItemType.SNOW_GLOVES), 1, new NorthStarShopPrice(10));
        sell(new SkyBlockItem(ItemType.SNOW_NECKLACE), 1, new NorthStarShopPrice(10));

        sell(minion(ItemType.ICE_MINION, 12), 1, price(300, new MinionShopPrice(ItemType.ICE_MINION, 11),
                new ItemShopPrice(ItemType.ENCHANTED_ICE, 1024), new CoinShopPrice(1_000_000)));
        sell(minion(ItemType.SNOW_MINION, 12), 1, price(500, new MinionShopPrice(ItemType.SNOW_MINION, 11),
                new ItemShopPrice(ItemType.ENCHANTED_SNOW_BLOCK, 1024), new CoinShopPrice(2_000_000)));

        sell(book(EnchantmentType.REFRIGERATE, 1), 1, price(10, new ItemShopPrice(ItemType.VOLCANIC_ROCK, 1)));
        sell(new SkyBlockItem(ItemType.MAGIC_TOP_HAT), 1, price(500, new ItemShopPrice(ItemType.VOLCANIC_ROCK, 10)));
        sell(new SkyBlockItem(ItemType.WINTER_FRAGMENT), 2, price(10, new ItemShopPrice(ItemType.VOLCANIC_ROCK, 1)));

        sell(book(EnchantmentType.LUCK, 6), 1, new NorthStarShopPrice(10));
        sell(book(EnchantmentType.LOOTING, 4), 1, new NorthStarShopPrice(10));
        sell(book(EnchantmentType.SCAVENGER, 4), 1, new NorthStarShopPrice(10));
    }

    private void talisman(ItemType talisman, ShopPrice price) {
        sell(new SkyBlockItem(talisman), 1, price).lockedWhen(_ -> isDecember() ? null : DECEMBER_ONLY);
    }

    private ShopItem sell(SkyBlockItem item, int amount, ShopPrice price) {
        ShopItem shopItem = new ShopItem(item, amount, price, false, false).withConfirmation();
        attachItem(shopItem);
        return shopItem;
    }

    private static CombinedShopPrice price(int northStars, ShopPrice... others) {
        List<ShopPrice> prices = new ArrayList<>(List.of(others));
        prices.add(new NorthStarShopPrice(northStars));
        return new CombinedShopPrice(prices);
    }

    private static SkyBlockItem minion(ItemType type, int tier) {
        SkyBlockItem minion = new SkyBlockItem(type);
        minion.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(tier, 0));
        return minion;
    }

    private static SkyBlockItem book(EnchantmentType type, int level) {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(type, level));
        return book;
    }
}
