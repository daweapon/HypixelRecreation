package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.dwarvenmines.puzzler.PuzzlerService;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCPuzzler extends HypixelNPC {

	private static final Pos POSITION = new Pos(181.5, 196, 135.5, 0, 0);

	public NPCPuzzler() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<d>Puzzler", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "BRTVu4zsbj0XqknzTreR1GnnGOuixTJAZVjdoSPAVI8LktsvT5Zh4nn8G6ziq4rWiHlltAU5GG3Z3TnXdlo0PV3iiyzjXmePHYg9qF95OLymNvGXoJXgmezKJiRjmJSpOiIzE0clEEqvHMh/p0h+4sxT6yJP9c2w+BBAiI8vVMzQeboTqcjkZzLb68GR1QeTsKWPMsQVpMeXyy9OMe2euSWt+oMQqtbujnzOQYLfvgLt05AKLl0Bl9c9w60eDIkyYEKmbF/tIDyaJ6yt8v8hmQ3xUOpjOO2foTkOCe161J60Au/o97/l0f0PEP5ZkdrnvPUm6wj+7uGuFpCJ3ibiOwUIUttnu6PCUozoFCl5SqdFtcQVjzwTm7PBwUHIN93oRYIkLXeglCJAa+Zkhi7kNz3zpYVxKxqHK2k4MKYATGRsxllCB4Jm+QgqN5U1R6x7y4+WaKpt6gpo7U3dO8ILtGmw/VTEJ5giUB4MH6eTxB0X6hLstxgt06Y5doUxSenk8UWIiCQ1ncbkEThxHtC+j9PEmK/MCQjm/LCWwHOgOg/m1U4P3RgrAd//PKI9CQ1RDGdVEWdBEeFEeCRvNE91xRj62gS226fnEYmEV1c3yqsl0N8s38q6O500xk81BqziQcfBV3MQAZE06VdRp+faiDJ5hL/aF59G5mhy/gVrkVo=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxMDU5MzY3MjkzNywKICAicHJvZmlsZUlkIiA6ICI5NGMzZGM3YTdiMmQ0NzQ1YmVlYjQzZDc2ZjRjNDVkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVRdWFzb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY1MTRkODAxNGRjNmFhNGM4MDRmZmRmNjA5MDkzZjdmZjFhNTI4OWYxM2M3MTg4MTQyMTgyZGI0M2M4OGIzZCIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return POSITION;
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (PuzzlerService.millisUntilReady(player) > 0) {
			say(player, "<f>▶▲Come▲ ◀back◀ ▼▶tomorrow▶▶");
			return;
		}
		PuzzlerService.Puzzle puzzle = PuzzlerService.generate(player, POSITION);
		if (puzzle == null) {
			say(player, "<f>╳ (╯°□°）╯︵ ┻━┻");
			return;
		}
		say(player, "<f>" + puzzle.arrows());
		player.sendMessage("<7>Start on the <e>Glowstone <7>block and face the room's entrance. Each arrow is one block. Mine the block you end on.");
	}

	public static void say(SkyBlockPlayer player, String text) {
		player.sendMessage(dev.daweapon.commons.text.Text.of("<e>[NPC] <d>Puzzler<f>: ").append(dev.daweapon.commons.text.Text.read(text)));
	}

}