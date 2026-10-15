package dev.daweapon.type.dwarvenmines.npcs;

import dev.daweapon.type.dwarvenmines.gui.GUIMinecart;
import dev.daweapon.type.dwarvenmines.rail.RailStop;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class AbstractTicketMaster extends AbstractTalkingNPC {
	private final RailStop stop;

	protected AbstractTicketMaster(String title, RailStop stop) {
		super(title, "<b>", "eyJ0aW1lc3RhbXAiOjE1NTQ1NzE2OTkxOTAsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NTJhMDZmN2I5OWU0ODIyYjQ5NWJhOWI2MDg5ZDRkNTE1MWU4M2JlZDk1NmE3Y2NjYjg0N2VhNDZhMjU3MjUifX19", stop.getPosition().withView(0, 0), "Hop on in and I'll get you going!");
		this.stop = stop;
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		((SkyBlockPlayer) event.player()).openView(new GUIMinecart(stop));
	}
}
