package dev.daweapon.type.replayviewer.event;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.replayviewer.TypeReplayViewerLoader;

public class PlayerLeaveEvent implements HypixelEventClass {
	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerDisconnectEvent event) {
		TypeReplayViewerLoader.getSession(event.getPlayer().getUuid()).ifPresent(session -> {
			session.removeViewer(event.getPlayer());
		});
	}
}
