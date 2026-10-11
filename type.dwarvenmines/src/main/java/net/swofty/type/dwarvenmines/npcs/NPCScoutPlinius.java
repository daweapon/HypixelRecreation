package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;

public class NPCScoutPlinius extends AbstractTalkingNPC {

	public NPCScoutPlinius() {
		super("Scout Plinius", "<9>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUxMDkxNTE0MDFkZTkzM2Q3OTNlYjNkYjljNGY5ZDlmNmM5ZTUyODdkZmE2YjY4MDkyMWNiNGQxM2JlNDliYzgifX19",
				new Pos(10, 121, 245, 0, 0),
				"Welcome to the Dwarven Base Camp, the gateway to the Glacite Tunnels!",
				"Out there you'll find Umber, Tungsten and Glacite, plus Aquamarine, Citrine, Onyx and Peridot gemstones.",
				"Mind the Cold though. Mine for too long and you'll freeze.");
	}
}
