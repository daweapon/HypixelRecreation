package net.swofty.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.swofty.type.bedwarslobby.BedWarsLobbyScoreboard;
import net.swofty.type.bedwarslobby.gui.cosmetics.ShopkeeperPreviewController;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.user.HypixelPlayer;

// Cleans up shopkeeper preview state and scoreboard cache when a lobby player disconnects.
public class ActionPlayerDisconnect implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerDisconnectEvent event) {
		HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		ShopkeeperPreviewController.stopPreview(player, null, null);
		BedWarsLobbyScoreboard.removeCache(player);
	}
}

