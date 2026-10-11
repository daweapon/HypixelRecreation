package net.swofty.type.dwarvenmines.npcs;

import net.swofty.type.dwarvenmines.gui.GUIMinecart;
import net.swofty.type.dwarvenmines.rail.RailStop;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class AbstractTicketMaster extends AbstractTalkingNPC {
	private final RailStop stop;

	protected AbstractTicketMaster(String title, RailStop stop) {
		super(title, "<9>", "eyJ0aW1lc3RhbXAiOjE1NTQ1NzE2OTkxOTAsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NTJhMDZmN2I5OWU0ODIyYjQ5NWJhOWI2MDg5ZDRkNTE1MWU4M2JlZDk1NmE3Y2NjYjg0N2VhNDZhMjU3MjUifX19", stop.getPosition().withView(0, 0), "Hop on in and I'll get you going!");
		this.stop = stop;
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		((SkyBlockPlayer) event.player()).openView(new GUIMinecart(stop));
	}
}
