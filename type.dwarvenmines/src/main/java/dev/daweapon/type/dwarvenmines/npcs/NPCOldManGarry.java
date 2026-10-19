package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.dwarvenmines.gui.GUIOldManGarry;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCOldManGarry extends HypixelNPC {
	public NPCOldManGarry() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<f>Old Man Garry", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NTQ1NzE2OTkxOTAsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NTJhMDZmN2I5OWU0ODIyYjQ5NWJhOWI2MDg5ZDRkNTE1MWU4M2JlZDk1NmE3Y2NjYjg0N2VhNDZhMjU3MjUifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(5.375, 200, -109.2, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		if (player.getToggles().get(dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GARRY)) {
			player.openView(new GUIOldManGarry());
			return;
		}
		setDialogue(player, "first").thenRun(() -> {
			player.getToggles().set(dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GARRY, true);
			player.openView(new GUIOldManGarry());
		});
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(DialogueSet.builder().key("first").lines(
				"<f>Events happen around the Dwarven Mines every 20 minutes.",
				"<f>Active ones need you to go somewhere, passive ones just work for everyone, and random ones turn up while you mine.",
				"<f>Talk to me whenever an event starts and I'll take you there."
		).build()).toArray(DialogueSet[]::new);
	}
}
