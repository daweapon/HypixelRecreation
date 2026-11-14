package dev.daweapon.type.ravengardgeneric.event.actions.player.data;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPlayerClearRavengardCache implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDisconnectEvent event) {
        // Reserved for non-persistent Ravengard runtime caches.
    }
}
