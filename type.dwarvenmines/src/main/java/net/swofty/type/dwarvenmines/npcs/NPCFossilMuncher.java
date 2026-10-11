package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCFossilMuncher extends AbstractTalkingNPC {

	public NPCFossilMuncher() {
		super("Fossil Muncher", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlOTA2Zjk3OTJlODc1MmRmNGU0MGE3NzdmY2Y1MDgxZGE3NWRkMGM0NmJkY2U3ODllMjEzMmM0OGE3NmFlYSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiI2NWI1NTI0M2UyYTYyMDRmODgwMTQzOTM0NjFmYzk3YiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzc3MDE0OTQ4MjMyfQ==",
				new Pos(-26.5, 125, 299.5, 0, 0));
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		var toggles = player.getToggles();
		if (toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FOSSIL_MUNCHER)) {
			setDialogue(player, "again");
			return;
		}
		setDialogue(player, "intro").thenRun(() -> toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FOSSIL_MUNCHER, true));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("intro").lines(
						"me so cold. me so hungry.",
						"u bring me fossil if u dont need them?"
				).build(),
				DialogueSet.builder().key("again").lines(
						"hmmm me no hungry",
						"come back l8r"
				).build()
		};
	}
}
