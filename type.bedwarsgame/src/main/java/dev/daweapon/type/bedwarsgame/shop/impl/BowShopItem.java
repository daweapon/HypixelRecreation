package dev.daweapon.type.bedwarsgame.shop.impl;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.ShopItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;

// Shop item for bows that applies a fixed enchantment list to the purchased bow.
public class BowShopItem extends ShopItem {

	final EnchantmentList enchantmentList;

	public BowShopItem(String id, String name, String description, int price, Currency currency, EnchantmentList enchantmentList) {
		super(id, name, description, price, 1, currency, Material.BOW, DatapointBedWarsHotbar.HotbarItemType.RANGED);
		this.enchantmentList = enchantmentList;
	}

	@Override
	public ItemStack getDisplay() {
		return super.getDisplay().builder().set(DataComponents.ENCHANTMENTS, enchantmentList).build();
	}

	@Override
	public void onPurchase(BedWarsPlayer player) {
		ItemStack bow = ItemStack.of(Material.BOW).builder().set(DataComponents.ENCHANTMENTS, enchantmentList).build();
		giveItem(player, bow);
	}
}
