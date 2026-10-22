package dev.daweapon.type.skyblockgeneric.event.actions.player;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.island.furniture.FurnitureSeat;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.player.PlayerInputEvent;

// Lets a player stand up from furniture by pressing sneak
public class ActionFurnitureDismount implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerInputEvent event) {
        if (!event.hasPressedShiftKey()) return;
        Entity vehicle = event.getPlayer().getVehicle();
        if (vehicle instanceof FurnitureSeat seat) seat.removePassenger(event.getPlayer());
    }
}
