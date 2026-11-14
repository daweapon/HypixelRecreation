package dev.daweapon.type.bedwarsgame.shop.impl;

import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.ShopItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;
import org.jetbrains.annotations.Nullable;

// Generic shop item that simply hands the player its display item stack on purchase.
public class BasicItem extends ShopItem {

	public BasicItem(String id, String name, String description, int cost, int amount, Currency currency, Material material) {
		super(id, name, description, cost, amount, currency, material);
	}

	public BasicItem(String id, String name, String description, int cost, int amount, Currency currency, Material material,
	                 @Nullable DatapointBedWarsHotbar.HotbarItemType hotbarItemType) {
		super(id, name, description, cost, amount, currency, material, hotbarItemType);
	}

	@Override
	public void onPurchase(BedWarsPlayer player) {
		giveItem(player, this.getDisplay().withAmount(this.getAmount()));
	}

}
