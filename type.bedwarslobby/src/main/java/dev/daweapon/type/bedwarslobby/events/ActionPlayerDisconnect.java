package dev.daweapon.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.bedwarslobby.BedWarsLobbyScoreboard;
import dev.daweapon.type.bedwarslobby.gui.cosmetics.ShopkeeperPreviewController;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;

// Cleans up shopkeeper preview state and scoreboard cache when a lobby player disconnects.
public class ActionPlayerDisconnect implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
	public void run(PlayerDisconnectEvent event) {
		HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		ShopkeeperPreviewController.stopPreview(player, null, null);
		BedWarsLobbyScoreboard.removeCache(player);
	}
}

