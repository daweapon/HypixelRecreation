package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityShop;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class AbiphoneElizabeth extends AbiphoneNPC {

	public AbiphoneElizabeth() {
		super("elizabeth", "<d>Elizabeth", "Open the Community Shop and hear when your upgrades finish!");
	}

	@Override
	public void onCall(HypixelPlayer player) {
		CommunityShop.open((SkyBlockPlayer) player);
	}

	@Override
	public ItemStack.Builder getIcon() {
		return ItemStacks.head("9017b6acf22deb1686293d7cccffe40901aa5aa4a6fa90f8e1217114e1d37302", Text.empty(), List.of());
	}
}
