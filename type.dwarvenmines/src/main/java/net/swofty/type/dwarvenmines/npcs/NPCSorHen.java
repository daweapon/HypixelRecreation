package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCSorHen extends AbstractTalkingNPC {

	public NPCSorHen() {
		super("Sor'Hen", "<b>",
				"ewogICJ0aW1lc3RhbXAiIDogMTYwODMxMzM3MzE3MywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjVmMzFjMWMyMTVhNTdlOTM3ZmQ3NWFiMzU3ODJmODVlYzI0MmExYjFmOTUwYTI2YTQyYmI1ZTBhYTVjYmVkYSIKICAgIH0KICB9Cn0=",
				new Pos(0.5, 121, 225.5, 0, 0));
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		var toggles = player.getToggles();
		if (toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SOR_HEN)) {
			setDialogue(player, "again");
			return;
		}
		setDialogue(player, "intro").thenRun(() -> {
			toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SOR_HEN, true);
			player.addAndUpdateItem(new SkyBlockItem(ItemType.SUSPICIOUS_SCRAP));
			setDialogue(player, "outro");
		});
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("intro").lines(
						"Ah, a fresh face in these parts!",
						"Ye've got the look of someone who ain't afraid to delve deep into the earth's belly.",
						"Name's Sor'Hen, and before ye ask, no, I'm not here for the camaraderie.",
						"I've got me eyes on the prize - ancient artifacts and the truth behind the lost king, Divan.",
						"Now, what's yer business here?",
						"Rare items, ye say?",
						"Well, ye've got the spirit of a true miner then.",
						"In these lands, the earth hides secrets and treasures older than any of us.",
						"Here, take this."
				).build(),
				DialogueSet.builder().key("outro").lines(
						"Found that beauty myself, deep in the shadows of those mines.",
						"It's but a glimpse of what's buried beneath.",
						"Suspicious Scrap are always surprising, may this one guide ye to riches and revelations.",
						"We'll cross paths again, I'm sure.",
						"The mines have a way of bringing like-minded souls together."
				).build(),
				DialogueSet.builder().key("again").lines(
						"We'll cross paths again, I'm sure.",
						"The mines have a way of bringing like-minded souls together."
				).build()
		};
	}
}
