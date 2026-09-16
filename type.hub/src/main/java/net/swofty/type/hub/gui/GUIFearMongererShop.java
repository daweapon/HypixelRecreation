package net.swofty.type.hub.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.List;

// The Fear Mongerer's real candy shop (Roadmap #19), opened by clicking the NPC directly - real
// Hypixel: "The Fear Mongerer is selling cool items in the Hub for a limited time!", a genuine
// direct-purchase shop separate from the point/leaderboard tier rewards (those are opened by
// clicking the leaderboard hologram instead - see GUIFearMongerer).
//
// Every item, price, and stat below was sourced directly from the user's own in-game tooltip
// screenshots of the real Fear Mongerer shop (not the wiki - its shop table is an image and
// didn't extract via automated fetch). Two exceptions, both intentionally left out rather than
// guessed: the base-tier Bat Talisman and Intimidation Talisman are referenced as upgrade
// ingredients by the Ring tier tooltips but were never themselves shown as purchasable in the
// screenshots (real Hypixel likely sources them from a mob drop instead) - not sold here. The
// Intimidation Relic (visible as a 4th icon in the shop grid) had no tooltip captured, so its
// price/stats are unconfirmed and it's left unsold rather than fabricated.
public class GUIFearMongererShop extends ShopView {

    public GUIFearMongererShop() {
        super("Fear Mongerer", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        // Trick or Treat Bag / Spirit Potion tooltips showed "Cost: Green Candy" with no "xN" -
        // read as x1, consistent with every other line here using an explicit "xN" when >1.
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.TRICK_OR_TREAT_BAG), 1,
                new ItemShopPrice(ItemType.GREEN_CANDY, 1)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SPIRIT_POTION_1), 1,
                new ItemShopPrice(ItemType.GREEN_CANDY, 1)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.CANDY_TALISMAN), 1,
                new ItemShopPrice(ItemType.GREEN_CANDY, 16)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.MAGIC_FIND_POTION_1), 1,
                new ItemShopPrice(ItemType.GREEN_CANDY, 10)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.BAT_RING), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.BAT_TALISMAN, 1),
                        new ItemShopPrice(ItemType.GREEN_CANDY, 64)))));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.CANDY_RING), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.CANDY_TALISMAN, 1),
                        new ItemShopPrice(ItemType.GREEN_CANDY, 64)))));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BAT_FIREWORK), 1,
                new ItemShopPrice(ItemType.GREEN_CANDY, 100)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.INTIMIDATION_RING), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.INTIMIDATION_TALISMAN, 1),
                        new ItemShopPrice(ItemType.GREEN_CANDY, 100)))));
        attachItem(ShopItem.Single(sugarRushBook(), 1,
                new CombinedShopPrice(List.of(
                        new CoinShopPrice(250_000),
                        new ItemShopPrice(ItemType.GREEN_CANDY, 64)))));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.HORSEMAN_CANDLE), 1,
                new ItemShopPrice(ItemType.PURPLE_CANDY, 32)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.CANDY_ARTIFACT), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.CANDY_RING, 1),
                        new ItemShopPrice(ItemType.PURPLE_CANDY, 32)))));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SPOOKY_HELMET), 1,
                new ItemShopPrice(ItemType.PURPLE_CANDY, 64)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SPOOKY_CHESTPLATE), 1,
                new ItemShopPrice(ItemType.PURPLE_CANDY, 64)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SPOOKY_LEGGINGS), 1,
                new ItemShopPrice(ItemType.PURPLE_CANDY, 64)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.SPOOKY_BOOTS), 1,
                new ItemShopPrice(ItemType.PURPLE_CANDY, 64)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.BAT_ARTIFACT), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.BAT_RING, 1),
                        new ItemShopPrice(ItemType.PURPLE_CANDY, 64)))));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.INTIMIDATION_ARTIFACT), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.INTIMIDATION_RING, 1),
                        new ItemShopPrice(ItemType.PURPLE_CANDY, 100)))));
        attachItem(ShopItem.Single(lifeStealBook(), 1,
                new CombinedShopPrice(List.of(
                        new CoinShopPrice(1_500_000),
                        new ItemShopPrice(ItemType.PURPLE_CANDY, 32)))));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.BLACK_CAT_PET), 1,
                new ItemShopPrice(ItemType.PURPLE_CANDY, 2000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.CANDY_RELIC), 1,
                new CombinedShopPrice(List.of(
                        new ItemShopPrice(ItemType.CANDY_ARTIFACT, 1),
                        new ItemShopPrice(ItemType.PURPLE_CANDY, 1024)))));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.ECHOLOCATOR), 1,
                new ItemShopPrice(ItemType.GREEN_CANDY, 32)));
    }

    private static SkyBlockItem sugarRushBook() {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.SUGAR_RUSH, 1));
        return book;
    }

    private static SkyBlockItem lifeStealBook() {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.LIFE_STEAL, 4));
        return book;
    }
}
