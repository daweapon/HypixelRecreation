package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.greatspook.DirtGuy;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCDirtGuy extends HypixelNPC {
	public NPCDirtGuy() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<f>Dirt Guy", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U2NzA2OWRmOTVjMjFhODZkMDkyYzAyYWJkYmI4YTE5NDYxZjM3MGYyOWMwZDAwMzQwMzdhNzIwOTZhNmExOWMifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(43.5, 108, 175.5, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		DirtGuy.interact((SkyBlockPlayer) event.player());
	}
}
