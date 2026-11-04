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
	public void onAdd(SkyBlockPlayer player, int slot) {
		if (dialogue().isInDialogue(player)) return;
		dialogue().setDialogue(player, "intro").thenRun(() -> {
			super.onAdd(player, slot);
			dialogue().setDialogue(player, "added");
		});
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("intro").lines(
						"<f>Hello!",
						"<f>Ooh, you got yourself an <3>Abiphone<f>!",
						"<f>Here, let me be one of your first <3>Abiphone Contacts<f>!",
						"<f>That way, you can call me anytime you'd like to use the <b>Community Shop<f>!").build(),
				DialogueSet.builder().key("added").lines(
						"<f>There you go!",
						"<f>I'll also be sure to call <o>you</o> <f>whenever one of your <d>Account & Profile Upgrades <f>are ready.",
						"<f>Ooh, and I'll also call you whenever a new <c><l>FIRE <c>Sale <f>starts! You wouldn't want to miss that!",
						"<f>See you around!").build()
		};
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
