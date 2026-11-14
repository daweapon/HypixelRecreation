package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.murdermysterygame.MurderMysteryGameScoreboard;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPlayerDisconnect implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
	public void run(PlayerDisconnectEvent event) {
		MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();

		Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
		if (game != null) {
			game.disconnect(player);
		}

		MurderMysteryGameScoreboard.removeCache(player);
	}
}
