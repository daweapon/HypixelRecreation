package dev.daweapon.type.bedwarsgame.events;

import net.minestom.server.event.player.PlayerHandAnimationEvent;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Records player arm-swing animations to the replay manager's entity lifecycle dispatcher.
public class ActionPlayerArmSwing implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerHandAnimationEvent event) {
        if (!(event.getPlayer() instanceof BedWarsPlayer player)) {
            return;
        }

        BedWarsGame game = player.getGame();
        if (game == null || game.getReplayManager() == null) {
            return;
        }

        if (!game.getReplayManager().isRecording()) {
            return;
        }

        var dispatcher = game.getReplayManager().getEntityLifecycleDispatcher();
        if (dispatcher != null) {
            boolean mainHand = event.getHand().name().equals("MAIN");
            dispatcher.recordArmSwing(player.getEntityId(), mainHand);
        }
    }
}
