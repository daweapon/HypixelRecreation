package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.dwarvenmines.gui.GUIDrStone;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCDrStone extends AbstractTalkingNPC {

	public NPCDrStone() {
		super("Dr. Stone", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdmZjEyY2MxMzdmMjAxODRkYjdiMGMyOWNlZTBkMWJlMjliY2FmMmNiZTUwNzAxYmQxMjkzYmQ1MDJlNjNiMGYifX19",
				new Pos(28, 120, 238.5, 0, 0));
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		var toggles = player.getToggles();
		if (toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DR_STONE)) {
			player.openView(new GUIDrStone());
			return;
		}
		setDialogue(player, "intro").thenRun(() -> {
			toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DR_STONE, true);
			player.addAndUpdateItem(new SkyBlockItem(ItemType.CHISEL));
			player.openView(new GUIDrStone());
		});
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{DialogueSet.builder().key("intro").lines(
				"Hello! I am Dr. Stone, a prominent Paleontologist from lands afar.",
				"If you ever find a fossil, bring it to me. Donate one and I'll study it, and you'll be able to turn it into a pet!",
				"Here, take this Chisel. Now you just need a Suspicious Scrap!"
		).build()};
	}
}
