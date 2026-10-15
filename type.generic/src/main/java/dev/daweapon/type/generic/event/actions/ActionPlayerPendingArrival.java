package dev.daweapon.type.generic.event.actions;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.arrival.PendingArrivals;

public class ActionPlayerPendingArrival implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.POST_SPAWN)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        Pos arrival = PendingArrivals.get().consume(event.getPlayer().getUuid());
        if (arrival != null) event.getPlayer().teleport(arrival);
    }
}
