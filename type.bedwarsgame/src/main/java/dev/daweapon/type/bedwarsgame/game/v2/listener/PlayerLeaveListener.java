package dev.daweapon.type.bedwarsgame.game.v2.listener;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.event.PlayerLeaveGameEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

import java.util.Random;

// Sends a leaving player back to the Bedwars lobby and announces their quit if still waiting.
public class PlayerLeaveListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onPlayerLeave(PlayerLeaveGameEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.player();
        player.sendTo(ServerType.BEDWARS_LOBBY);
        BedWarsGame game = (BedWarsGame) event.game();
        if (game == null) return;
        if (!game.getState().isWaiting()) return;

        game.broadcastMessage(Text.of("<k>{} <e>has quit!",
            player.getFakeUuid().toString().substring(0, new Random().nextInt(10) + 4)));
    }
}
