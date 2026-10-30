package dev.daweapon.type.dwarvenmines.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointFrozenCorpses;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseMilestones;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseProgress;
import dev.daweapon.type.skyblockgeneric.gui.inventories.corpse.GUIFrozenCorpseMilestones;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class NPCGrandpaWolf extends HypixelNPC {
	private static final String SKIN_HASH = "8745c3249a354aafc50dea7369640497d5e591a34e21990df5bc95c2799413e1";
	private static final String SKIN_TEXTURE = Base64.getEncoder().encodeToString(
			("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + SKIN_HASH + "\"}}}").getBytes(StandardCharsets.UTF_8));

	public NPCGrandpaWolf() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<f>Grandpa Wolf", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return SKIN_TEXTURE;
			}

			@Override
			public Text chatNameText(HypixelPlayer player) {
				return Text.of("<6>Grandpa Wolf");
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-55.5, 125, 331.5, 0, 0);
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
		if (CorpseProgress.data(player).isMetWolf()) {
			player.openView(new GUIFrozenCorpseMilestones());
			return;
		}
		setDialogue(player, "first").thenRun(() -> askToHelp(player));
	}

	private void askToHelp(SkyBlockPlayer player) {
		choose(player, "grandpa_wolf_help", () -> setDialogue(player, "accepted").thenRun(() -> askIfKnown(player)),
				() -> setDialogue(player, "declined"));
	}

	private void askIfKnown(SkyBlockPlayer player) {
		choose(player, "grandpa_wolf_known", () -> setDialogue(player, "knows").thenRun(() -> meet(player)),
				() -> setDialogue(player, "explains"));
	}

	private void meet(SkyBlockPlayer player) {
		DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
		data.setMetWolf(true);
		CorpseProgress.save(player, data);
		CorpseMilestones.claimReached(player).forEach(line -> player.sendMessage(line));
		player.openView(new GUIFrozenCorpseMilestones());
	}

	private static void choose(SkyBlockPlayer player, String id, Runnable yes, Runnable no) {
		Consumer<HypixelPlayer> onYes = _ -> yes.run();
		Consumer<HypixelPlayer> onNo = _ -> no.run();
		NPCOption.sendOption(player, id, true, List.of(
				new NPCOption.Option("yes", NamedTextColor.GREEN, true, "Yes", onYes),
				new NPCOption.Option("no", NamedTextColor.RED, true, "No", onNo)));
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder().key("first").lines(
						"<f>Oh, <7>" + player.getUsername() + "<f>! I'm having a conundrum! There's so many corpses scattered around the <b>Glacite Mineshafts<f>, and so little time!",
						"<f>Tell you what, would you be willing to help me gather them all for me? I'll <6>reward <7>you handsomely!",
						"<f>...So long as I get to keep the corpses."
				).build(),
				DialogueSet.builder().key("accepted").lines(
						"<f>Awesome! Okay, stand still real quick...",
						"<f><o>*sniff sniff*",
						"<f>Okay, now I'm familiar with your <a>scent<f>!",
						"<f>Using my amazing power of smell, I'll track you as you move through the <b>Glacite Mineshafts<f>.",
						"<f>...",
						"<f>You know what the <b>Glacite Mineshafts <f>are, right?"
				).build(),
				DialogueSet.builder().key("declined").lines(
						"<f>Oh, that's okay.",
						"<f>My wife's not gonna be happy.",
						"<f>Been out here for months without so much as a scrap to show for it...",
						"<f>You know, you're starting to look kinda <o>tasty<f>..."
				).build(),
				DialogueSet.builder().key("knows").lines(
						"<f>Great, the look on your face had me worried for a second!",
						"<f>Anyways, whenever you loot a <b>Frozen Corpse<f>, I'll come soon after to retrieve it.",
						"<f>Come talk to me and I'll show you how many corpses we, the dream team, have gathered together!",
						"<f>After reaching certain <b>Frozen Corpse Milestones <f>I'll even give you some useful stuff from my stash!",
						"<f>Here, take a look!"
				).build(),
				DialogueSet.builder().key("explains").lines(
						"<f>Hmm, maybe you're not ready then, yet.",
						"<b>Glacite Mineshafts <f>sometimes pop up while you're mining in the <b>Glacite Tunnels<f>.",
						"<f>If you enter one, you'll fine <b>Frozen Corpses <f>strewn throughout the place - they're all that's left of some unfortunate dwarves from long ago!",
						"<f>Though they're not very talkative fellows on account of being dead 'n all, you can still have a rummage through their backpacks for some loot!",
						"<f>Now, why don't you go and find a <b>Glacite Mineshafts <f>or two, and then come talk to me again when you're ready for us to partner up?"
				).build()
		).toArray(DialogueSet[]::new);
	}
}
