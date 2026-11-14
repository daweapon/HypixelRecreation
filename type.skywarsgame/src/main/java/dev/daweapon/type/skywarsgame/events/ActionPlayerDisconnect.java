package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.SkywarsGameScoreboard;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionPlayerDisconnect implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void onDisconnect(PlayerDisconnectEvent event) {
        SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);

        if (game != null) {
            game.disconnect(player);
        }
        // git is wierd with this file idk
        SkywarsGameScoreboard.removeCache(player);
    }
}
