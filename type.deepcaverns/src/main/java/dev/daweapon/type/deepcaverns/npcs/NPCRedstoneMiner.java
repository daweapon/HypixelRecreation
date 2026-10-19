package dev.daweapon.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.deepcaverns.DeepCavernsQuests;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.function.Consumer;

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

	private static final Sound VOICE = Sound.sound(Key.key("entity.villager.yes"), Sound.Source.PLAYER, 1f, 1f);
	private static final String OPTION_ID = "redstone_miner";
	private static final String[] PROMPTS = {
			"If you can bring me <c>128 Redstone<f>, I'll reward you with an extra slot for your <a>Accessory Bag<f>.",
			"If you can bring me <c>256 Redstone<f>, I'll reward you with <d>another slot <f>for your <a>Accessory Bag<f>.",
			"This time I'll need <c>512 Redstone <f>- that's 8 stacks! It's quite a lot, but I know you can do it.",
			"This time, I will need <c>1,024 Redstone<f>. That's 16 stacks, if you didn't know!"
	};

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;

		int tier = DeepCavernsQuests.redstoneTier(player);
		if (DeepCavernsQuests.redstoneFinished(tier)) {
			setDialogue(player, "finished-" + (1 + (int) (Math.random() * 2)));
			return;
		}
		if (DeepCavernsQuests.redstoneOffer(player) == DeepCavernsQuests.REDSTONE_OFFER_ACCEPTED) {
			prompt(player, tier);
			return;
		}
		if (DeepCavernsQuests.collectionTier(player, ItemType.REDSTONE) < DeepCavernsQuests.REDSTONE_COLLECTION_TIER) {
			setDialogue(player, "first");
			return;
		}
		boolean declinedBefore = DeepCavernsQuests.redstoneOffer(player) == DeepCavernsQuests.REDSTONE_OFFER_DECLINED;
		setDialogue(player, declinedBefore ? "again" : "already").thenRun(() -> NPCOption.sendOption(player, OPTION_ID, true, List.of(
				option("yes", NamedTextColor.GREEN, "YES", true, this::accept),
				option("no", NamedTextColor.RED, "NO", true, this::decline))));
	}

	private void accept(HypixelPlayer who) {
		SkyBlockPlayer player = (SkyBlockPlayer) who;
		DeepCavernsQuests.setRedstoneOffer(player, DeepCavernsQuests.REDSTONE_OFFER_ACCEPTED);
		setDialogue(player, "yes");
	}

	private void decline(HypixelPlayer who) {
		SkyBlockPlayer player = (SkyBlockPlayer) who;
		DeepCavernsQuests.setRedstoneOffer(player, DeepCavernsQuests.REDSTONE_OFFER_DECLINED);
		setDialogue(player, "no");
	}

	private void prompt(SkyBlockPlayer player, int tier) {
		sendNPCMessage(player, PROMPTS[tier], VOICE);
		NPCOption.sendOption(player, OPTION_ID, true, List.of(
				option("give", NamedTextColor.GREEN, "Give materials", false, this::give),
				option("no", NamedTextColor.RED, "No thanks!", false, this::later)));
	}

	private void give(HypixelPlayer who) {
		SkyBlockPlayer player = (SkyBlockPlayer) who;
		int tier = DeepCavernsQuests.redstoneTier(player);
		if (DeepCavernsQuests.redstoneFinished(tier) || isInDialogue(player)) return;
		int cost = DeepCavernsQuests.redstoneCost(tier);
		if (player.getAmountInInventory(ItemType.REDSTONE) < cost) {
			sendNPCMessage(player, "Come back with the right amount of items!", VOICE);
			return;
		}
		player.takeItem(ItemType.REDSTONE, cost);
		DeepCavernsQuests.setRedstoneTier(player, tier + 1);
		setDialogue(player, "reward-" + (tier + 1)).thenRun(() ->
				player.sendMessage("<7>You got an extra <a>Accessory Bag<7> slot from helping the <e>Redstone Miner<a>!"));
	}

	private void later(HypixelPlayer who) {
		sendNPCMessage(who, "Alright, come back later when you want to give the items!", VOICE);
	}

	private static NPCOption.Option option(String key, NamedTextColor color, String name, boolean bold, Consumer<HypixelPlayer> action) {
		return new NPCOption.Option(key, color, bold, name, action);
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("first").lines(
						"<c>Redstone <f>is such an amazing resource that can be used for so many different purposes.",
						"For example, by leveling up your <a>Redstone Collection<f>, you can unlock more slots in your <a>Accessory Bag<f>!",
						"Reach <a>Redstone Collection II <f>to unlock the <a>Small Accessory Bag <f>upgrade, then talk to me again!"
				).build(),
				DialogueSet.builder().key("already").lines(
						"Oh wait, you already have <a>Redstone Collection II<f>!",
						"In that case, would you like to help me out with something? In return, I can give you some <d>more slots <f>for your <a>Accessory Bag<f>."
				).build(),
				DialogueSet.builder().key("again").lines(
						"Filling your <a>Accessory Bag <f>grants you <6>Accessory Power<f>. If you speak to my friend <6>Maxwell <f>in the <b>Village<f>, he can channel that power into giving you bonus stats!",
						"For now, would you like to help me out with something? In return, I can give you some <d>more slots <f>for your <a>Accessory Bag<f>."
				).build(),
				DialogueSet.builder().key("yes").lines(
						"Thank you, it's much appreciated!",
						"If you can bring me <c>128 Redstone<f>, I'll reward you with an extra slot for your <a>Accessory Bag<f>.",
						"You can find <c>Redstone <f>all around the <b>Pigmen's Den<f>, but the Pigmen don't really appreciate you stealing it.",
						"They roughed me up real good earlier, so I'm a bit worried about mining any myself. But, you look strong enough to go for it!"
				).build(),
				DialogueSet.builder().key("no").lines(
						"Understandable, have a nice day. Please speak to me again if you ever change your mind."
				).build(),
				DialogueSet.builder().key("reward-1").lines(
						"Amazing! Thanks for the <c>Redstone<f>!",
						"Just as I promised, here's an extra <a>Accessory Bag <f>slot!",
						"You can view your <a>Accessory Bag <f>within the <a>SkyBlock Menu<f>.",
						"If you can bring me <c>256 Redstone<f>, I'll reward you with <d>another slot <f>for your <a>Accessory Bag<f>."
				).build(),
				DialogueSet.builder().key("reward-2").lines(
						"Thank you very much!",
						"Now, as I promised, take another <d>slot<f>!",
						"We're not done yet though; This time I'll need <c>512 Redstone <f>- that's 8 stacks! It's quite a lot, but I know you can do it."
				).build(),
				DialogueSet.builder().key("reward-3").lines(
						"That's the most <c>Redstone <f>I've ever seen in one place! And it's all for me...",
						"Please, take another <a>Accessory Bag <f>slot! You deserve it!",
						"If you have any energy left, I have just one more task for you. Finally, I will need <c>1,024 Redstone<f>. That's 16 stacks, if you didn't know.",
						"I know it's a lot, so feel free to come back later if you're busy."
				).build(),
				DialogueSet.builder().key("reward-4").lines(
						"Great work! Here's your last <a>Accessory Bag <f>slot, you've earned it!",
						"With this much <c>Redstone<f>, there's no end to what I can do! The sky's the limit!",
						"I hope you've come to appreciate <c>Redstone <f>as much as I do.",
						"That's all I can do for you! If you still want more <a>Accessory Bag <f>slots, talk to <6>Jacobus <f>in the <b>Village<f>, or simply level up your <a>Redstone Collection<f>!"
				).build(),
				DialogueSet.builder().key("finished-1").lines(
						"The Pigmen around here are actually quite passive. They'll only attack if you or someone nearby mines too much <c>Redstone<f>.",
						"That's all I can do for you.",
						"If you still want more <a>Accessory Bag <f>slots, talk to <6>Jacobus <f>in the <b>Village<f>, or simply level up your <a>Redstone Collection<f>!"
				).build(),
				DialogueSet.builder().key("finished-2").lines(
						"That's all I can do for you! If you still want more <a>Accessory Bag <f>slots, talk to <6>Jacobus <f>in the <b>Village<f>, or simply level up your <a>Redstone Collection<f>!"
				).build()
		};
	}
}
