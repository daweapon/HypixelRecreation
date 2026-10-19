package dev.daweapon.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.deepcaverns.DeepCavernsQuests;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.Map;

// NPC for Rhys, found in the Deep Caverns.
public class NPCRhys extends HypixelNPC {

	public NPCRhys() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Rhys", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "cEhw4KPnjOLHodZV5sBF0cDMUcmzFklYNbv8zR3G1+IwdkEfLxnXeSOeZRu3OrGOvxn6WwZWl/P4cZbfAxYEoVXvTYFesNM8lI0VNwuqt0QGz5sjkKMlQlEbdQilnw2Ki77SAqFqd8XHknYuUku/XpMEFCvDaS96SwTkQRwitE8EhJb5/X9AS6m/Wslu/F/AuKcZKc5UIXFr6geGV0XhJUpW8rBnWPxf4so5XKdQLXPvM8aActwAecXV4MkIPTsP1N6nOywEOeV5qs56AgoNgx44u+38Xg+0RV0wr9hzYHENPos7ZSfdVwd/tqAMTbyrKj4AGppl7MnSynEdgiBBk22qiF3g8vYgGj4O9w3h5185U9Ow7f2DihIy3mDAiFSvc77Hx/BGYkIxZ4HXWrKRj3INTxVSalB1RG7tuCHs4J/xAaDu2yI5zSedAX+L6VXeudIzpUPym5JaRc3alL1TW94ZQZ4W7QKw5fViLVORr4lve2fdDaz9KRUVkGeQx9L7Q81LaCV84E/8j0juzyHMtVdm+fmDERr+TSoa7REk6KIg0bpTqvyWiqJyygL8/sw02SuhqAupFdf3QOTLbLrh4qU8LtMvykl9V7n36uSTRZi0EVJasODGEVF2Qyt+toFSRdIvc6YUY+C2UJf3jI1C7E9hAuz42ayleKdGoezAsds=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxNDY5NDY2OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmIyMGIyM2MxYWEyYmUwMjcwZjAxNmI0YzkwZDZlZTZiODMzMGExN2NmZWY4Nzg2OWQ2YWQ2MGIyZmZiZjNiNSIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(31.5, 12, 14.5, -90, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	private static final Sound YES = Sound.sound(Key.key("entity.villager.yes"), Sound.Source.PLAYER, 1f, 1f);

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;

		if (DeepCavernsQuests.rhysComplete(player)) {
			setDialogue(player, "idle");
			return;
		}
		if (!DeepCavernsQuests.miningLevelReached(player)) {
			setDialogue(player, "level");
			return;
		}

		int delivered = DeepCavernsQuests.rhysOres(player);
		Map<ItemType, Integer> plan = DeepCavernsQuests.planRhysHandIn(delivered, player::getAmountInInventory);
		if (plan.isEmpty()) {
			setDialogue(player, delivered == 0 ? "ask" : "need");
			return;
		}

		int given = 0;
		for (Map.Entry<ItemType, Integer> entry : plan.entrySet()) {
			if (player.takeItem(entry.getKey(), entry.getValue()) != null) given += entry.getValue();
		}
		DeepCavernsQuests.setRhysOres(player, delivered + given);
		if (DeepCavernsQuests.rhysComplete(player)) {
			setDialogue(player, "complete").thenRun(() ->
					player.getAchievementHandler().addProgressByTrigger("skyblock.resourceful_trigger", 1));
			return;
		}
		sendNPCMessage(player, "Thanks for the items! I still need <a>" + (DeepCavernsQuests.RHYS_ORES_REQUIRED - delivered - given)
				+ " <f>more.", YES);
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("level").lines("Come back when you are Mining Level XII!").build(),
				DialogueSet.builder().key("ask").lines(
						"So you want to see the Dwarven Mines?",
						"Bring me <a>15 <f>enchanted ores. Any mix of Enchanted Redstone, Iron Ingot, Coal, Emerald, Lapis Lazuli, Diamond or Gold Ingot will do."
				).build(),
				DialogueSet.builder().key("need").lines(
						"I still need more enchanted ores. Redstone, Iron Ingot, Coal, Emerald, Lapis Lazuli, Diamond and Gold Ingot all count."
				).build(),
				DialogueSet.builder().key("complete").lines(
						"Perhaps you are someone we can get along with after all.",
						"The Dwarven Mines and your <5>Heart of the Mountain <f>are open to you. Take the Lift Operator down whenever you are ready."
				).build(),
				DialogueSet.builder().key("idle").lines(
						"The Dwarven Mines are open to you now. Use the Lift Operator to get there."
				).build()
		};
	}
}
