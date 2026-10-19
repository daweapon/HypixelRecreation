package dev.daweapon.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.deepcaverns.DeepCavernsQuests;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

// NPC for the Lapis Miner, found in the Deep Caverns.
public class NPCLapisMiner extends HypixelNPC {

	public NPCLapisMiner() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Lapis Miner", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "SissRoKQWTHkTCNRnTMHimwaoqOA3OcENRzAEOAU+WZ+1NKJXqehyd6ntO3HyCzit9FlJSBigpCFKYEykR/yAhaFbyb9sbVv+HvO2OvttHkme+Nx9upGVOz7LeB+1tSiQtGEZgGqhOUxCppFsYWrVKUmGMSWf6TCnAv5TJHx1yjy0OgY5NpTaUOKW95egGKZEzbBUAvC7bi8n8Bfpv42yuYnAoMPQAQ2RvxRqNFNmTPsxy10y8x7v/GqxOIeC9gbBsklUE/UKEdyBE0I+fCn7MP80b94N0aAl5qIHH84/yzf1WMna4XRmywYoAf1MmH5Scl51RLeRwuCuwKobgGXTyViHtFkX+ELGrLOvw9SFRp98cvdr1ghoC7oyn3yaxXYC20Bz+OjOcq7LBn+Bq8NJk1yTZ4ePYVtNUZewV8kQpJL+zs6WvEiEh7raVF57BTaU+dkZ1BIW/BNv2xk3VZHR73lYxucI5oyT58mSUGZjUrsl6p9jirEXrc+iU9/pdtObV/CzWr53rMNNPZTLfMCrDYW+1a0YmRXPhYQ4KCOiZhH3utr1M58o8xNiw1Kf061ZCluA1LKV1CYyM0EffL2HADooDggpaq17Q6W6yScNqnROShL+sU6Aq3H+Sj2pa3KIni/OLRpBH+HodV/gZyh+MfTj73+uSZ9JCiC7dZkMkY=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY5MzkzMDgxMjc2NCwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JmYWFmNWRlMGQyNWFlMjZiMjg3YTJiYTUxOTYxYjk2OGEzYWRkOGJiYzI0NzJlOTdhNzZmZGM2OGNlNDFmNzgiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-10.5, 120, 35.5, -115, 0);
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

		int state = DeepCavernsQuests.lapisMinerState(player);
		if (state == DeepCavernsQuests.LAPIS_DONE) {
			setDialogue(player, "idle");
			return;
		}

		if (state == DeepCavernsQuests.LAPIS_UNTOUCHED) {
			DeepCavernsQuests.setLapisMinerState(player, DeepCavernsQuests.LAPIS_ASKED);
			setDialogue(player, "first");
			return;
		}

		if (DeepCavernsQuests.collectionTier(player, ItemType.LAPIS_LAZULI) < DeepCavernsQuests.LAPIS_COLLECTION_TIER) {
			setDialogue(player, "collection");
			return;
		}

		if (player.getAmountInInventory(ItemType.LAPIS_PICKAXE) < 1) {
			setDialogue(player, "craft");
			return;
		}

		player.takeItem(ItemType.LAPIS_PICKAXE, 1);
		DeepCavernsQuests.setLapisMinerState(player, DeepCavernsQuests.LAPIS_DONE);
		setDialogue(player, "thanks").thenRun(() -> player.addAndUpdateItem(new SkyBlockItem(ItemType.COMPACTOR)));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("first").lines(
						"Hey you! I broke my Lapis Pickaxe - do you think you could bring me another?",
						"First you'll need to reach <9>Lapis Lazuli Collection III<f>, and then you can craft it for me."
				).build(),
				DialogueSet.builder().key("collection").lines(
						"Some items in SkyBlock can only be crafted when you reach a certain collection requirement.",
						"To craft me a Lapis Pickaxe, you will need to reach <9>Lapis Lazuli Collection III<f>."
				).build(),
				DialogueSet.builder().key("craft").lines(
						"Oh! You reached <9>Lapis Lazuli Collection III<f>!",
						"Any chance you can craft me a new <9>Lapis Pickaxe <f>now?",
						"Check the <a>Recipe Book <f>in your SkyBlock Menu to learn how to craft it!"
				).build(),
				DialogueSet.builder().key("thanks").lines(
						"Thank you! I appreciate your support in these trying times.",
						"For your trouble, here's a <9>Compactor<f>. Place it in a minion upgrade slot and it will automatically turn items into their block form once it's collected enough of them.",
						"I use it in my Lapis Minion, those things fill terribly fast!"
				).build(),
				DialogueSet.builder().key("idle").lines(
						"My Lapis Pickaxe might not mine the fastest, but it gives me a lot of experience!",
						"I use that experience to enchant my Lapis Pickaxe with efficiency, which makes it mine faster!",
						"How cool is that?"
				).build()
		};
	}
}
