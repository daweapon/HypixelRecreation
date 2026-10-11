package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.glacite.RailroadPass;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCDulin extends AbstractTalkingNPC {

	public NPCDulin() {
		super("Dulin", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3NGMxMjRhMmQ1NDM1OWMyZmRkMDg2Mjk4ODcwNjYwODkzNWU2MGVmNzQ1ODg2N2E5MGJlMDEyMjM3ZjVkZjgifX19",
				new Pos(15.5, 128, 206.5, 0, 0));
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		var toggles = player.getToggles();
		if (player.countItem(ItemType.SECRET_RAILROAD_PASS) > 0) {
			setDialogue(player, "pass");
		} else if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DULIN) && !RailroadPass.isUnlocked(player)) {
			setDialogue(player, "intro").thenRun(() -> toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DULIN, true));
		} else if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DULIN_AT_CAMP)) {
			setDialogue(player, "camp").thenRun(() -> toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_DULIN_AT_CAMP, true));
		} else {
			setDialogue(player, "again");
		}
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		String user = player.getUsername();
		return new DialogueSet[]{
				DialogueSet.builder().key("intro").lines(
						"This is all that remains...",
						"Hello, " + user + ". Thank you for coming.",
						"I have heard of your triumphs in the Crystal Hollows, and thought we should meet.",
						"I am Dulin, descendant of Divan and rightful King of the Dwarven Mines. Those imbeciles in the palace took up their positions in my absence.",
						"I am preparing an expedition into the area beyond the Great Ice Wall, and need some adventurers to scout out the area for me.",
						"Thank you. My people will remember your name for eons.",
						"I am glad that I have your trust, but I will need you to prove yourself before entering the Glacite Tunnels.",
						"The Ticket Master will only let you in if you have a Secret Railroad Pass to show him.",
						"You can put one together in the Dwarven Forge, if you can get the right materials."
				).build(),
				DialogueSet.builder().key("pass").lines(
						"A Secret Railroad Pass! Very well.",
						"Go show that to the Ticket Master, and he'll let you travel to the Glacite Tunnels.",
						"I'll see you down there!"
				).build(),
				DialogueSet.builder().key("camp").lines(
						"Nice to see you here, " + user + ".",
						"Feel free to go on ahead, but don't stray too far into the Glacite Tunnels.",
						"From what I've heard, they go very deep down. The legends speak of some dwarves finding Mineshafts when mining!",
						"Anyways, if you need me I'll be here, waiting for my Expedition Team to arrive.",
						"They should be here within 3-5 business days, then we can set out!"
				).build(),
				DialogueSet.builder().key("again").lines(
						"Hey " + user + "! Have you found time to explore the Glacite Tunnels yet?",
						"The team should arrive shortly! They should be here within 3-5 business days, then we can set out!"
				).build()
		};
	}
}
