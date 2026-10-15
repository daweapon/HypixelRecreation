package dev.daweapon.type.ravengarddungeon.events;

import dev.daweapon.type.game.game.event.PlayerPostJoinGameEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengarddungeon.game.RavengardDungeonGame;
import dev.daweapon.type.ravengarddungeon.user.RavengardDungeonPlayer;

// On successfully joining the dungeon game, places the player into the instance at their assigned spawn point.
public class PlayerJoinGameListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onPlayerJoinGame(PlayerPostJoinGameEvent event) {
        RavengardDungeonPlayer player = (RavengardDungeonPlayer) event.player();
        int playerIndex = event.currentPlayerCount() - 1;
        player.setInstance(event.getGame().getInstance(),
                RavengardDungeonGame.SPAWN_POINTS.get(playerIndex % RavengardDungeonGame.SPAWN_POINTS.size()));
    }
}
