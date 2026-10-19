package dev.daweapon.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.deepcaverns.DeepCavernsQuests;

// NPC for the Redstone Miner, found in the Deep Caverns.
public class NPCRedstoneMiner extends HypixelNPC {

	public NPCRedstoneMiner() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Redstone Miner", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "wJTZ9MCUjX/mtBuzZcey1VVlywKqV91n8hWUGyrR/37p+gCntLgGzjssYYWqKRWgVosrIPjtye5E23fYMv3lW+SEPdu3o14kjYl+ZPzXLJ/A3Kt8aJO+8TJthGzXGm2fKb3PTmvQVIvAxaoOzDwP9Q8JjXgZcUCoLtY1IGlcAlr4xWqUQNV3UsbNeWG/1W9otKgqrvX9oKBn+ScSQMPItIasG6VKRj/22S/kSlzgkS7ZdewpM6Yzk/sT2srvkKQuM561ZCX1AMq7xhr+OdJRg3zEX3G6PTVDBWk8pWPvTYvCVpkreEfh47WiFRGweWmB4lvTWITDj9dyOHwG1M08bR4w1oVOUkN+RzA7X1/4vSlwOIJIf0s/1JH+hmM1bIvGWl46ZqxitKUyq6/NfSFtWVnlXNshz1+5/eZ8DYGfKUtKiJQvxTU86mZ0kdeUiys97WB8Qs/x/nVoxgz61+LAAp9lQ3EetVgnfyUvJc8aegTnIuJwt18SoPB3w9RTZU40BE7EcXjUE/tcQ7aD/f7MTGhqNokfNmsGatLxOpi8Jg7Z9dKkXpYUIqRP2wm/5ahHfMfQYAamDVTEblpgfJcaEwMUlPlwOjHcwrlTi5a1DjA6s9V9d3SalYEtn+a+YrP51Ib4NhGMXVyE4jOQstMdzaG4JKpJYiNHmwNwU/4QY58=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY5MzkzMDkxNTIyMiwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTUwM2MwMDg5MDMyNjYwNTA2N2ZmMTZmZmZiYzJkMDUwMjI1MWIzNjgwYzFhY2MxZDY4Y2QzZDA2NGQwNTc3IgogICAgfQogIH0KfQ==";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(25, 104, 16.5, -66, 0);
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

		int tier = DeepCavernsQuests.redstoneTier(player);
		if (DeepCavernsQuests.redstoneFinished(tier)) {
			setDialogue(player, "finished");
			return;
		}

		if (DeepCavernsQuests.collectionTier(player, ItemType.REDSTONE) < DeepCavernsQuests.REDSTONE_COLLECTION_TIER) {
			setDialogue(player, "collection");
			return;
		}

		int cost = DeepCavernsQuests.redstoneCost(tier);
		if (!DeepCavernsQuests.redstoneAccepted(player)) {
			DeepCavernsQuests.setRedstoneAccepted(player, true);
			setDialogue(player, "offer-" + tier);
			return;
		}

		if (player.getAmountInInventory(ItemType.REDSTONE) < cost) {
			setDialogue(player, "short");
			return;
		}

		player.takeItem(ItemType.REDSTONE, cost);
		DeepCavernsQuests.setRedstoneTier(player, tier + 1);
		DeepCavernsQuests.setRedstoneAccepted(player, false);
		setDialogue(player, "reward-" + (tier + 1));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("collection").lines(
						"Redstone is such an amazing resource that can be used for so many different purposes.",
						"Reach <c>Redstone Collection II <f>and I will make you a deal for some Accessory Bag slots."
				).build(),
				DialogueSet.builder().key("offer-0").lines(
						"Redstone is such an amazing resource that can be used for so many different purposes.",
						"The Pigmen do not like it when Redstone gets mined, and I am too hurt to mine it myself.",
						"If you can bring me <c>128 Redstone<f>, I'll reward you with an extra slot for your <9>Accessory Bag<f>."
				).build(),
				DialogueSet.builder().key("offer-1").lines(
						"Back for more? Next up I'll need <c>256 Redstone <f>- that's 4 stacks - for another <9>Accessory Bag <f>slot."
				).build(),
				DialogueSet.builder().key("offer-2").lines(
						"This time I'll need <c>512 Redstone <f>- that's 8 stacks! It's quite a lot, but I know you can do it."
				).build(),
				DialogueSet.builder().key("offer-3").lines(
						"Finally, I will need <c>1,024 Redstone<f>. That's 16 stacks, if you didn't know."
				).build(),
				DialogueSet.builder().key("short").lines("Come back with the right amount of items!").build(),
				DialogueSet.builder().key("reward-1").lines(
						"Just as I promised, here's an extra <9>Accessory Bag <f>slot!",
						"Talk to me again when you are ready for the next one."
				).build(),
				DialogueSet.builder().key("reward-2").lines(
						"Just as I promised, here's an extra <9>Accessory Bag <f>slot!",
						"Talk to me again when you are ready for the next one."
				).build(),
				DialogueSet.builder().key("reward-3").lines(
						"Just as I promised, here's an extra <9>Accessory Bag <f>slot!",
						"Talk to me again when you are ready for the next one."
				).build(),
				DialogueSet.builder().key("reward-4").lines(
						"Great work! Here's your last <9>Accessory Bag <f>slot, you've earned it!",
						"If you still want more Accessory Bag slots, talk to Jacobus in the Village, or simply level up your Redstone Collection!"
				).build(),
				DialogueSet.builder().key("finished").lines(
						"If you still want more Accessory Bag slots, talk to Jacobus in the Village, or simply level up your Redstone Collection!"
				).build()
		};
	}
}
