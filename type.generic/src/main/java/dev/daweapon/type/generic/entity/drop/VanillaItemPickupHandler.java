package dev.daweapon.type.generic.entity.drop;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class VanillaItemPickupHandler implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Instance instance = player.getInstance();
        if (instance == null) return;

        instance.getEntityTracker().nearbyEntities(player.getPosition(), ItemDropPhysics.PICKUP_SCAN_RANGE,
                EntityTracker.Target.ITEMS, item -> {
                    if (!(item instanceof VanillaItemEntity dropped)) return;
                    if (!ItemPickup.isWithinRange(player, dropped)) return;
                    ItemPickup.pickup(player, dropped);
                });
    }
}
