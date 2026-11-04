package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.miningevent.CultOfTheFallenStar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class AbstractCultMember extends HypixelNPC {
	private final boolean leader;

	protected AbstractCultMember(String name, Pos position, String texture, String signature, boolean leader) {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return leader ? new String[]{"<5>" + name, "<e><l>CLICK"} : new String[]{"<5>" + name};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return signature;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return texture;
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
		this.leader = leader;
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		if (leader) CultOfTheFallenStar.interact((SkyBlockPlayer) event.player());
	}
}
