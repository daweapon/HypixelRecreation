package dev.daweapon.type.bedwarsgame.game.v2.listener;

import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.event.PlayerRejoinGameEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Restores a reconnecting player's state into their in-progress Bedwars game.
public class PlayerRejoinGameListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onPlayerGameRejoin(PlayerRejoinGameEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.player();
        BedWarsGame game = player.getGame();

        game.onPlayerRejoin(player, event.disconnectedPlayerData());
    }

}
