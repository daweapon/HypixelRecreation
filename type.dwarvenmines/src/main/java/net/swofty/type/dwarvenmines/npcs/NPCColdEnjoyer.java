package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;

public class NPCColdEnjoyer extends AbstractTalkingNPC {

	public NPCColdEnjoyer() {
		super("Cold Enjoyer", "<9>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM4YTJmYjA2ZDQ3Nzc2MDY2OTQ0ODdmNWI2ZjI5MDc2ZmY2YzA5Y2Y0NTE5MWI5YjE2Njk2Y2Q5OTYxYTg4NzYifX19",
				new Pos(-15.25, 136.78125, 217.25, 0, 0),
				"Ahh, the cold. Nothing quite like it!",
				"Out in the Glacite Tunnels, the cold builds up on you the longer you stay.",
				"The colder you get, the slower you mine. Cold Resistance slows it down.",
				"Reach -100 Cold and you'll freeze to death, so watch yourself!",
				"Come back to the Base Camp to warm up. Your Cold resets as soon as you step inside.");
	}
}
