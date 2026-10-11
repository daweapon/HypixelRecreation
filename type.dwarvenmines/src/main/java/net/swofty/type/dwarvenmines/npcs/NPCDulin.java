package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.glacite.RailroadPass;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCDulin extends AbstractTalkingNPC {

	public NPCDulin() {
		super("Dulin", "<9>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3NGMxMjRhMmQ1NDM1OWMyZmRkMDg2Mjk4ODcwNjYwODkzNWU2MGVmNzQ1ODg2N2E5MGJlMDEyMjM3ZjVkZjgifX19",
				new Pos(15.5, 128, 206.5, 0, 0),
				"The Glacite Tunnels stretch deep beneath the mountain, full of Umber, Tungsten and Glacite.");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
		if (RailroadPass.meetsRequirement(skyBlockPlayer)) {
			return new DialogueSet[]{DialogueSet.builder().key("talk").lines(
					"The Glacite Tunnels stretch deep beneath the mountain, full of Umber, Tungsten and Glacite.",
					"To ride the minecart out here from the Dwarven Village you will need a Secret Railroad Pass.",
					"Forge one at the Forge, then show it to a Ticket Master or Station Master."
			).build()};
		}
		return new DialogueSet[]{DialogueSet.builder().key("talk").lines(
				"The Glacite Tunnels stretch deep beneath the mountain.",
				"But you're not strong enough to be out here yet. Your Heart of the Mountain needs to reach Tier 7."
		).build()};
	}
}
