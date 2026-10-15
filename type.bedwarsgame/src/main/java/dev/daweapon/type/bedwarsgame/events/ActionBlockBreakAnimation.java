package dev.daweapon.type.bedwarsgame.events;

import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import dev.daweapon.type.bedwarsgame.TypeBedWarsGameLoader;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Forwards block-break animation packets into the replay recorder while a game is being recorded.
public class ActionBlockBreakAnimation implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, isAsync = true) // should it be async? - ARI
    public void run(PlayerPacketOutEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        BedWarsGame game = TypeBedWarsGameLoader.getGameById(player.getGameId());
        if (game == null || !game.getReplayManager().isRecording()) {
            return;
        }

        ServerPacket packet = event.getPacket();
        if (packet instanceof BlockBreakAnimationPacket p) {
            game.getReplayManager().recordBlockBreakAnimation(p);
        }
    }

}
