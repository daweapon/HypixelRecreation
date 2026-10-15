package dev.daweapon.type.bedwarsgame.shop.impl;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.ShopItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;

// Shop item for wool blocks, giving the buyer wool colored to their own team.
public class Wool extends ShopItem {

	public Wool() {
		super("wool", "Wool", "Great for bridging across islands.\nTurns into your team's color.", 4, 16, Currency.IRON, Material.WHITE_WOOL,
			DatapointBedWarsHotbar.HotbarItemType.BLOCKS);
	}

	@Override
	public void onPurchase(BedWarsPlayer player) {
		giveItem(player, ItemStack.builder(player.getTeamKey().woolMaterial())
				.amount(16)
				.build());
	}

}
