package dev.daweapon.type.hub.gui;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for the Librarian NPC in the Hub, selling books and basic enchanted books.
public class GUIShopLibrarian extends ShopView {
    public GUIShopLibrarian() {
        super("Librarian", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(Material.EXPERIENCE_BOTTLE), 1, new CoinShopPrice(30)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BOOK), 1, new CoinShopPrice(20)));

        SkyBlockItem sharpness = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        sharpness.getAttributeHandler().addEnchantment(
                new SkyBlockEnchantment(EnchantmentType.SHARPNESS, 1)
        );

        SkyBlockItem scavenger = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        scavenger.getAttributeHandler().addEnchantment(
                new SkyBlockEnchantment(EnchantmentType.SCAVENGER, 1)
        );

        SkyBlockItem protection = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        protection.getAttributeHandler().addEnchantment(
                new SkyBlockEnchantment(EnchantmentType.PROTECTION, 1)
        );

        SkyBlockItem efficiency = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        efficiency.getAttributeHandler().addEnchantment(
                new SkyBlockEnchantment(EnchantmentType.EFFICIENCY, 1)
        );

        attachItem(ShopItem.Single(sharpness, 1, new CoinShopPrice(30)));
        attachItem(ShopItem.Single(scavenger, 1, new CoinShopPrice(40)));
        attachItem(ShopItem.Single(protection, 1, new CoinShopPrice(20)));
        attachItem(ShopItem.Single(efficiency, 1, new CoinShopPrice(20)));

    }
}
