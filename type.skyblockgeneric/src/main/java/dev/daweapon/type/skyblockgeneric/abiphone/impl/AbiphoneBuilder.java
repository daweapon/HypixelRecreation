package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.gui.inventories.builder.GUIBuilder;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.List;

// Abiphone contact for the Builder NPC, requiring a donated Builder's Wand before unlocking his contact.
public class AbiphoneBuilder extends AbiphoneNPC {

	public AbiphoneBuilder() {
		super("builder", "Builder", "Need to build? Call him.");
	}

	@Override
	public void onCall(HypixelPlayer player) {
		new GUIBuilder().open(player);
	}

	@Override
	public void onAdd(SkyBlockPlayer player, int slot) {
		if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_GIVEN_BUILDER_BUILDERS_WAND)) {
			if (dialogue().isInDialogue(player)) return;
			dialogue().setDialogue(player, "abiphone").thenRun(() -> {
				NPCOption.sendOption(player, "builder", false, Collections.singletonList(new NPCOption.Option(
						"pay",
						NamedTextColor.GREEN,
						true,
						"GIVE ITEM",
						(p) -> {
							SkyBlockPlayer sp = (SkyBlockPlayer) p;
							for (int i = 0; i < sp.getInventory().getSize(); i++) {
								ItemStack item = sp.getInventory().getItemStack(i);
								if (new SkyBlockItem(item).getItemType() == ItemType.BUILDERS_WAND) {
									sp.getInventory().setItemStack(i, ItemStack.of(Material.AIR));
									dialogue().setDialogue(player, "donate").thenRun(() -> {
										player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_GIVEN_BUILDER_BUILDERS_WAND, true);
										super.onAdd(player, slot);
									});
									return;
								}
							}
							dialogue().setDialogue(player, "donate_no_requirements");

						}
				)));
			});
			return;
		}
		super.onAdd(player, slot);
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder()
						.key("abiphone").lines(
								"Oh?",
								"Yes... I could give you my contact... However, may I ask a favor in return?",
								"For a builder to not have a <6>Builder's Wand <f>is quite uncommon.",
								" If you give me one I'll gladly give you my contact!"
						).build(),
				DialogueSet.builder()
						.key("donate").lines(
								"A <6>Builder's Wand<f>! This is a dream come true!"
						).build(),
				DialogueSet.builder()
						.key("donate_no_requirements").lines(
								"You don't have what I need! Nice try!"
						).build()
		};
	}

	@Override
	public ItemStack.Builder getIcon() {
		return ItemStacks.head("c8ccd4fdf58b30aa83017cfa5fed977196c024c8d1a276004e9068e8ecbb0b79", Text.empty(), List.of());
	}
}
