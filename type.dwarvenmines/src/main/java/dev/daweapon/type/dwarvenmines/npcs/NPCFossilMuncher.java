package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.fossil.FossilService;
import dev.daweapon.type.skyblockgeneric.fossil.FossilType;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NPCFossilMuncher extends AbstractTalkingNPC {

	public NPCFossilMuncher() {
		super("Fossil Muncher", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlOTA2Zjk3OTJlODc1MmRmNGU0MGE3NzdmY2Y1MDgxZGE3NWRkMGM0NmJkY2U3ODllMjEzMmM0OGE3NmFlYSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiI2NWI1NTI0M2UyYTYyMDRmODgwMTQzOTM0NjFmYzk3YiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzc3MDE0OTQ4MjMyfQ==",
				new Pos(-26.5, 125, 299.5, 0, 0));
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
		if (System.currentTimeMillis() < data.getFossilMunchReadyAt()) {
			setDialogue(player, "cooldown");
			return;
		}
		FossilType request = request(player, data);
		var toggles = player.getToggles();
		if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FOSSIL_MUNCHER)) {
			setDialogue(player, "intro").thenRun(() -> toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FOSSIL_MUNCHER, true));
			return;
		}
		SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
		ItemType heldType = held.getAttributeHandler().getPotentialType();
		FossilType heldFossil = heldType == null ? null : FossilType.byItem(heldType);
		if (heldFossil == request && player.removeItemsFromPlayer(Map.of(request.getItem(), 1))) {
			feed(player, data);
			setDialogue(player, "fed");
		} else if (heldFossil != null) {
			setDialogue(player, "wrongFossil");
		} else if (heldType != null) {
			setDialogue(player, "notFossil");
		} else {
			setDialogue(player, "hint");
		}
	}

	private static FossilType request(SkyBlockPlayer player, DatapointHOTM.PlayerHOTMData data) {
		FossilType[] all = FossilType.values();
		FossilType current = data.getFossilMunchRequest() == null ? null : FossilType.valueOf(data.getFossilMunchRequest());
		if (current != null) return current;
		FossilType picked = all[ThreadLocalRandom.current().nextInt(all.length)];
		data.setFossilMunchRequest(picked.name());
		HotmService.save(player, data);
		return picked;
	}

	private static void feed(SkyBlockPlayer player, DatapointHOTM.PlayerHOTMData data) {
		data.addGlacitePowder(FossilService.MUNCHER_POWDER);
		data.setFossilMunchRequest(null);
		data.setFossilMunchReadyAt(System.currentTimeMillis() + FossilService.MUNCHER_COOLDOWN_MILLIS);
		HotmService.save(player, data);
		Essences.add(player, EssenceType.FOSSIL, FossilService.MUNCHER_ESSENCE);
		player.sendMessage("<a>You received <b>{} Glacite Powder <a>and <d>{} Fossil Essence<a>!",
				String.format("%,d", FossilService.MUNCHER_POWDER), FossilService.MUNCHER_ESSENCE);
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
		DatapointHOTM.PlayerHOTMData data = HotmService.data(skyBlockPlayer);
		String hint = data.getFossilMunchRequest() == null ? "..." : FossilType.valueOf(data.getFossilMunchRequest()).getMuncherHint();
		return new DialogueSet[]{
				DialogueSet.builder().key("intro").lines("me so cold. me so hungry.", "u bring me fossil if u dont need them?", hint).build(),
				DialogueSet.builder().key("hint").lines("me so cold. me so hungry.", hint).build(),
				DialogueSet.builder().key("notFossil").lines("errrr", hint, "that is not a fossil", hint).build(),
				DialogueSet.builder().key("wrongFossil").lines("mmm nope not hungry for that fossil.", hint).build(),
				DialogueSet.builder().key("fed").lines("mmm quite tasty.", "take powder", "u feed me more fossil tomorrow?").build(),
				DialogueSet.builder().key("cooldown").lines("hmmm me no hungry", "come back l8r").build()
		};
	}
}
