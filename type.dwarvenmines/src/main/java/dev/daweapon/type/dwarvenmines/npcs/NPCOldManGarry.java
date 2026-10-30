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
				return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkyMTg2N2Y0ZWM3YWZmZjA4OTI2NzFlYWMzOTlhY2RjYzYzMTA1ZDBhZTQ0NjJhNDhhNDNhZjVlY2JhMWU0ZDUifX19";
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
