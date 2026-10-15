package dev.daweapon.type.goldmine.gui;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

import java.util.Map;

// Shop GUI for the Iron Forger, selling iron gear and ingots in the Gold Mine.
public class GUIShopIronForger extends ShopView {
    public GUIShopIronForger() {
        super("Iron Forger", DEFAULT);
    }

    private SkyBlockItem applyEnchantment (SkyBlockItem item, Map<EnchantmentType, Integer> enchantments) {
        ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
        enchantments.forEach(((enchantmentType, level) ->
                itemAttributeHandler.addEnchantment(new SkyBlockEnchantment(enchantmentType, level))));
        return itemAttributeHandler.asSkyBlockItem();
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.IRON_INGOT), 20, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(applyEnchantment(new SkyBlockItem(Material.CHAINMAIL_HELMET), Map.of(EnchantmentType.PROTECTION, 1, EnchantmentType.GROWTH, 1)), 1, new CoinShopPrice(50)));
        attachItem(ShopItem.Single(applyEnchantment(new SkyBlockItem(Material.CHAINMAIL_CHESTPLATE), Map.of(EnchantmentType.PROTECTION, 1, EnchantmentType.GROWTH, 1)), 1, new CoinShopPrice(100)));
        attachItem(ShopItem.Single(applyEnchantment(new SkyBlockItem(Material.CHAINMAIL_LEGGINGS), Map.of(EnchantmentType.PROTECTION, 1, EnchantmentType.GROWTH, 1)), 1, new CoinShopPrice(75)));
        attachItem(ShopItem.Single(applyEnchantment(new SkyBlockItem(Material.CHAINMAIL_BOOTS), Map.of(EnchantmentType.PROTECTION, 1, EnchantmentType.GROWTH, 1)), 1, new CoinShopPrice(50)));
        attachItem(ShopItem.Single(applyEnchantment(new SkyBlockItem(Material.IRON_PICKAXE), Map.of(EnchantmentType.SMELTING_TOUCH, 1)), 1, new CoinShopPrice(60)));
    }
}
