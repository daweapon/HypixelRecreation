package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.GUIShopBrynmor;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBrynmor extends HypixelNPC {

	public NPCBrynmor() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<9>Brynmor", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTc3MjY0MmZmY2NmYzllMTFiMzUwYzg3NGYyYzg0Njc4ZmMwODA0NGI1MWU3YThlM2EwOTE5ZjhmNzg4ZWQ5YSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJiZTFmMjBiZTE5ODkyZDg0YmRhNjU0ZTI2OWI4MzIyNiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzkxNjk0MDQ1ODQyfQ==";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(32.5, 202, -154.5, 0, 0);
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
		var toggles = player.getToggles();
		if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRYNMOR)) {
			setDialogue(player, "hello").thenRun(() -> toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRYNMOR, true));
			return;
		}
		player.openView(new GUIShopBrynmor());
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{DialogueSet.builder().key("hello").lines(
				"Welcome to the tavern!",
				"We're known best for our <a>Powder Pie<f>!",
				"Please enjoy yer stay!"
		).build()};
	}
}
