package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.election.ElectionView;

import java.util.List;

// Abiphone contact for Clerk Seraphine, letting players cast their Mayor Election vote remotely
// instead of visiting the Community Center in person. Matches real Hypixel: no unlock requirement,
// calling opens the voting UI directly.
public class AbiphoneClerkSeraphine extends AbiphoneNPC {

	public AbiphoneClerkSeraphine() {
		super("clerk_seraphine", "<b>Clerk Seraphine", "Cast your vote remotely for the upcoming Mayor Election!");
	}

	@Override
	public void onCall(HypixelPlayer player) {
		player.openView(new ElectionView());
	}

	@Override
	public ItemStack.Builder getIcon() {
		return ItemStacks.head("cd654531816ec7200a6a461d7a03c2dadf31f449a591c85f3b31cb2a83dd7366", Text.empty(), List.of());
	}
}
