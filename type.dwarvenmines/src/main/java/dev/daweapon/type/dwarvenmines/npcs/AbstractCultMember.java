package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.miningevent.CultOfTheFallenStar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class AbstractCultMember extends HypixelNPC {
	private static final String PLACEHOLDER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkyMTg2N2Y0ZWM3YWZmZjA4OTI2NzFlYWMzOTlhY2RjYzYzMTA1ZDBhZTQ0NjJhNDhhNDNhZjVlY2JhMWU0ZDUifX19";

	protected AbstractCultMember(String name, Pos position) {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<5>" + name, "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return PLACEHOLDER_TEXTURE;
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return position;
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}

			@Override
			public boolean visible(HypixelPlayer player) {
				return CultOfTheFallenStar.meetingActive();
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		CultOfTheFallenStar.interact((SkyBlockPlayer) event.player());
	}
}
