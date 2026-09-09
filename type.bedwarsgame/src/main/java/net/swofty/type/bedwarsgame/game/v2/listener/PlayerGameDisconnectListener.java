package net.swofty.type.bedwarsgame.game.v2.listener;

import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.event.PlayerDisconnectGameEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.event.HypixelEventClass;

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
