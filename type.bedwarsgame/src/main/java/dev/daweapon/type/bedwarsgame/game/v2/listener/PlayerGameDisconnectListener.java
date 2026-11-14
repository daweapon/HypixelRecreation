package dev.daweapon.type.bedwarsgame.game.v2.listener;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.event.PlayerDisconnectGameEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Broadcasts a team-colored disconnect message when a player leaves a Bedwars game mid-match.
public class PlayerGameDisconnectListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onPlayerDisconnect(PlayerDisconnectGameEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.player();
        BedWarsGame game = player.getGame();
        if (game != null) {
            Text name = game.getPlayerTeam(player.getUuid())
                .map(team -> Text.of("<color:{}>{}", team.getColor(), player.getUsername()))
                .orElseGet(() -> Text.literal(player.getUsername()));

            game.broadcastMessage(Text.of("<7>{} disconnected.", name));
        }
    }

}
