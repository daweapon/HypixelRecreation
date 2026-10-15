package dev.daweapon.type.ravengardlobby.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Teleports a player back to spawn if they fall below y=0 in the Ravengard lobby.
public class ActionPlayerVoid implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        if (event.getPlayer().getPosition().y() < 0) {
            event.getPlayer().teleport(HypixelConst.getTypeLoader()
                    .getLoaderValues()
                    .spawnPosition()
                    .apply(HypixelConst.getTypeLoader().getType())
            );
        }
    }
}
