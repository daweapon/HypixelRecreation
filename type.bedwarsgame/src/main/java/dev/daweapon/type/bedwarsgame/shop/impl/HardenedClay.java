package dev.daweapon.type.bedwarsgame.shop.impl;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.ShopItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;

// Shop item that gives the buyer's team-colored terracotta blocks.
public class HardenedClay extends ShopItem {

	public HardenedClay() {
		super("hardened_clay", "Hardened Clay", "Basic block to defend your bed.", 12, 16, Currency.IRON, Material.TERRACOTTA,
			DatapointBedWarsHotbar.HotbarItemType.BLOCKS);
	}

	private Material mapTeamToTerracotta(BedWarsMapsConfig.TeamKey teamKey) {
		return switch (teamKey) {
			case RED -> Material.RED_TERRACOTTA;
			case BLUE -> Material.BLUE_TERRACOTTA;
			case GREEN -> Material.GREEN_TERRACOTTA;
			case YELLOW -> Material.YELLOW_TERRACOTTA;
			case AQUA -> Material.LIGHT_BLUE_TERRACOTTA;
			case PINK -> Material.PINK_TERRACOTTA;
			case WHITE -> Material.WHITE_TERRACOTTA;
			case GRAY -> Material.GRAY_TERRACOTTA;
		};
	}

	@Override
	public void onPurchase(BedWarsPlayer player) {
		Material terracottaMaterial = mapTeamToTerracotta(player.getTeamKey());
		giveItem(player, ItemStack.builder(terracottaMaterial)
				.amount(16)
				.build());
	}

}
