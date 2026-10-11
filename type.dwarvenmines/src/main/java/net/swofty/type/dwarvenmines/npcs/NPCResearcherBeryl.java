package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUIFossilEssenceShop;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCResearcherBeryl extends AbstractTalkingNPC {

	public NPCResearcherBeryl() {
		super("Researcher Beryl", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTVlYzVlMmQ1NTcyM2M5ODdjZTYwYjg5MTEzN2VlMzk5NTk4ZTE1ZWY5MTBjNzU0MmQzYzMxNmZlYzY2MjY0ZCIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiI3MzBmOTg4YmNhMTYyYmIzOTZiYzAyMDNkNGYzMWU5MSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzc3MDE0NDIzOTU3fQ==",
				new Pos(35.5, 128, 236, 0, 0));
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		var toggles = player.getToggles();
		if (toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RESEARCHER_BERYL)) {
			player.openView(new GUIFossilEssenceShop());
			return;
		}
		setDialogue(player, "intro").thenRun(() -> {
			toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RESEARCHER_BERYL, true);
			player.openView(new GUIFossilEssenceShop());
		});
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{DialogueSet.builder().key("intro").lines(
				"Oh, a visitor! Nice to meet you!",
				"I'm out here collecting Fossil Essence for our studies.",
				"You can get Fossil Essence by excavating the various blocks of scrap in the Fossil Excavator, or by mining Fossil Blocks in the Glacite Mineshafts.",
				"Bring me Fossil Essence, and I'll trade it with you for useful perks and items!"
		).build()};
	}
}
