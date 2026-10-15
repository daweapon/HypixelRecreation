package dev.daweapon.type.lobby.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Teleports players back to spawn if they fall out of the lobby world's bounds.
public class LobbyPlayerMove implements HypixelEventClass {
	private final Pos spawnPoint;

	public LobbyPlayerMove(Pos spawnPoint) {
		this.spawnPoint = spawnPoint;
	}

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player.getPosition().y() < 0) {
			player.teleport(spawnPoint);
			player.sendMessage("<c>You are not allowed to leave this area!");
		}
	}
}
