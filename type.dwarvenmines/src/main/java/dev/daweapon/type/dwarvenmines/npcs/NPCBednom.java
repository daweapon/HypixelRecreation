package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.bednom.BednomQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBednom extends HypixelNPC {
	public NPCBednom() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<d>Bednom", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY1ZjMxYzFjMjE1YTU3ZTkzN2ZkNzVhYjM1NzgyZjg1ZWMyNDJhMWIxZjk1MGEyNmE0MmJiNWUwYWE1Y2JlZGEifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-29.5, 213.843, -88.5, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		BednomQuest.talk((SkyBlockPlayer) event.player());
	}
}
