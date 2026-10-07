package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.spray.CanvasRoomPainter;

// Drops a player's Canvas Room painting when they leave the server
public class ActionCanvasRoomForget implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        CanvasRoomPainter.forget(event.getPlayer().getUuid());
    }
}
