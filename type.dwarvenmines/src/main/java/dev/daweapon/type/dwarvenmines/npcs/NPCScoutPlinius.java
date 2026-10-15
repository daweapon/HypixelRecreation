package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class NPCScoutPlinius extends AbstractTalkingNPC {

	public NPCScoutPlinius() {
		super("Scout Plinius", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUxMDkxNTE0MDFkZTkzM2Q3OTNlYjNkYjljNGY5ZDlmNmM5ZTUyODdkZmE2YjY4MDkyMWNiNGQxM2JlNDliYzgifX19",
				new Pos(10, 121, 245, 0, 0),
				"Are you %USER%?",
				"Nice to meet you. I'm Scout Plinius, a Dwarven Scout.",
				"Are you about to head into the Glacite Tunnels?",
				"Well, try to keep track of where you are!",
				"If you ever get a bit too Cold you can always warm yourself up over at the Campfire!",
				"Try to watch your step, word around is that there are a lot of old Glacite Mineshafts hidden around this place.");
	}

	@Override
	protected String[] lines(HypixelPlayer player) {
		return java.util.Arrays.stream(super.lines(player)).map(line -> line.replace("%USER%", player.getUsername())).toArray(String[]::new);
	}
}
