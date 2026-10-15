package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.jspecify.annotations.NonNull;

public abstract class AbstractTalkingNPC extends HypixelNPC {
	private final String[] lines;

	protected AbstractTalkingNPC(String name, String color, String texture, Pos pos, String... lines) {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{color + name, "<e><l>CLICK"};
			}

			@Override
			public String texture(HypixelPlayer player) {
				return texture;
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return pos;
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}

			@Override
			public @NonNull String chatName(HypixelPlayer player) {
				return color + name;
			}
		});
		this.lines = lines;
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		HypixelPlayer player = event.player();
		if (isInDialogue(player)) return;
		setDialogue(player, "talk");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{DialogueSet.builder().key("talk").lines(lines(player)).build()};
	}

	protected String[] lines(HypixelPlayer player) {
		return lines;
	}
}
