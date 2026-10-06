package net.swofty.type.skyblockgeneric.event.actions.entity;

import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.VisitManager;

// Forces the damage-flash animation packet whenever an entity takes damage. Island visitors are
// immune to all damage, as on Hypixel since patch 0.6.
public class ActionAnimateEntityDamage implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityDamageEvent event) {
        if (event.getEntity() instanceof SkyBlockPlayer player && VisitManager.isVisiting(player.getUuid())) {
            event.setCancelled(true);
            return;
        }

        event.setAnimation(true);
        event.setCancelled(false);

        event.getEntity().sendPacketToViewersAndSelf(new DamageEventPacket(event.getEntity().getEntityId(),
                        0,
                        0,
                        0,
                        null));
    }
}
