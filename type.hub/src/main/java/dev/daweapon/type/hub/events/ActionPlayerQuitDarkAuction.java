package dev.daweapon.type.hub.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Handles cleanup when a player disconnects while in the Dark Auction.
 * This ensures the service is notified and local caches are cleared.
 */
public class ActionPlayerQuitDarkAuction implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        // Let the handler deal with cleanup and service notification
        DarkAuctionHandler.handlePlayerLeft(player);
    }
}
