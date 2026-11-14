package dev.daweapon.type.bedwarsgame.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.bedwarsgame.BedWarsGameScoreboard;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.GameState;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Handles player disconnects during a Bedwars game, marking them as offline or removing them.
public class ActionPlayerDisconnect implements HypixelEventClass {

	@SneakyThrows
	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerDisconnectEvent event) {
		final BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
		BedWarsGame game = player.getGame();
		if (game != null) {
			if (game.getState() == GameState.IN_PROGRESS) {
				game.handleDisconnect(player);
			} else {
				game.leave(player);
			}
		}
		BedWarsGameScoreboard.removeCache(player);
	}
}

