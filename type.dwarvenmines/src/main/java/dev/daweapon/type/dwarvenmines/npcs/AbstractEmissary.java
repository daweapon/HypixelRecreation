package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.commission.GUICommisions;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.jspecify.annotations.NonNull;

public abstract class AbstractEmissary extends HypixelNPC {

	public AbstractEmissary(String name, String texture, String signature, Pos pos) {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Emissary " + name, "<e><l>CLICK"};
			}

			@Override
			public String texture(HypixelPlayer player) {
				return texture;
			}

			@Override
			public String signature(HypixelPlayer player) {
				return signature;
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
				return Text.of("<6>{}", name).serialize();
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		HypixelPlayer player = event.player();
		new GUICommisions().open(player);
	}
}
