package dev.daweapon.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.deepcaverns.DeepCavernsQuests;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import net.minestom.server.item.ItemStack;

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
			public Text chatNameText(HypixelPlayer player) {
				return Text.of("<d>Rhys");
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

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;

		if (DeepCavernsQuests.rhysComplete(player)) {
			setDialogue(player, "idle-" + (1 + (int) (Math.random() * 2)));
			return;
		}
		if (!DeepCavernsQuests.miningLevelReached(player)) {
			setDialogue(player, "level");
			return;
		}

		ItemStack held = player.getItemInMainHand();
		if (held.isAir()) {
			setDialogue(player, "ask");
			return;
		}
		SkyBlockItem heldItem = new SkyBlockItem(held);
		ItemType heldType = heldItem.getAttributeHandler().getPotentialType();
		int delivered = DeepCavernsQuests.rhysOres(player);
		Map<ItemType, Integer> plan = DeepCavernsQuests.planRhysHandIn(delivered,
				type -> type == heldType ? held.amount() : 0);
		if (plan.isEmpty()) {
			setDialogue(player, "wrong");
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
		setDialogue(player, "thanks");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("level").lines(
						"So you want to visit the <2>Dwarven Mines<f>?",
						"You must prove yourself worthy of <5>Heart of the Mountain<f>...",
						"You are no match for the Dwarves of the Deeper Caverns.",
						"Come back when you are Mining Level XII!"
				).build(),
				DialogueSet.builder().key("ask").lines(
						"So you want to visit the <2>Dwarven Mines<f>?",
						"You must prove yourself worthy of <5>Heart of the Mountain<f>...",
						"You must bring me some valuable materials that peak my interest.",
						"I would like a total of 15 items. Any from this list will do:",
						"<8>- <a>Enchanted Redstone Dust",
						"<8>- <a>Enchanted Iron Ingot",
						"<8>- <a>Enchanted Coal",
						"<8>- <a>Enchanted Emerald",
						"<8>- <a>Enchanted Lapis Lazuli",
						"<8>- <a>Enchanted Diamond",
						"<8>- <a>Enchanted Gold Ingot"
				).build(),
				DialogueSet.builder().key("wrong").lines(
						"That's not an item I'm looking for!",
						"I would like a total of 15 items. Any from the list will do:",
						"<8>- <a>Enchanted Redstone Dust",
						"<8>- <a>Enchanted Iron Ingot",
						"<8>- <a>Enchanted Coal",
						"<8>- <a>Enchanted Emerald",
						"<8>- <a>Enchanted Lapis Lazuli",
						"<8>- <a>Enchanted Diamond",
						"<8>- <a>Enchanted Gold Ingot"
				).build(),
				DialogueSet.builder().key("thanks").lines("Thank you for the items!").build(),
				DialogueSet.builder().key("complete").lines(
						"Perhaps you are someone we can get along with after all.",
						"I'll get your <5>Heart of the Mountain<f>. It's time for a visit!",
						"Let's go together now. For future trips, remember the <b>Lift Operator<f>."
				).build(),
				DialogueSet.builder().key("idle-1").lines("I had a sunburn once, I have stayed down there ever since.").build(),
				DialogueSet.builder().key("idle-2").lines("You can ask the <b>Lift Operator <f>for a ride to the <2>Dwarven Mines<f>!").build()
		};
	}
}
