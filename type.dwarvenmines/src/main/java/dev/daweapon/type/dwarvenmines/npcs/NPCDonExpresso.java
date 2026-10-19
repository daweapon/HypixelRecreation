package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.miningevent.MithrilGourmandEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCDonExpresso extends HypixelNPC {
	public NPCDonExpresso() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<b>Don Expresso", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return null;
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NTQ1NzE2OTkxOTAsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NTJhMDZmN2I5OWU0ODIyYjQ5NWJhOWI2MDg5ZDRkNTE1MWU4M2JlZDk1NmE3Y2NjYjg0N2VhNDZhMjU3MjUifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return MithrilGourmandEvent.donPosition();
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}

			@Override
			public boolean visible(HypixelPlayer player) {
				return MithrilGourmandEvent.active() != null;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		MithrilGourmandEvent gourmand = MithrilGourmandEvent.active();
		if (gourmand == null) return;
		gourmand.feed((SkyBlockPlayer) event.player());
	}
}
