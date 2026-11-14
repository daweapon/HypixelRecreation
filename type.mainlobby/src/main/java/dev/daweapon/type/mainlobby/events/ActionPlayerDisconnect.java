package dev.daweapon.type.mainlobby.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.mainlobby.MainLobbyScoreboard;

// Clears a player's cached Main Lobby scoreboard on disconnect.
public class ActionPlayerDisconnect implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerDisconnectEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        MainLobbyScoreboard.removeCache(player);
    }
}

