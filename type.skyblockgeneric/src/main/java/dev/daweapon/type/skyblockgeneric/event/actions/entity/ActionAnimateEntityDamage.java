package dev.daweapon.type.skyblockgeneric.event.actions.entity;

import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoDamageProtection;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HollowsSpawnProtection;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.CreeperVeil;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

// Forces the damage-flash animation packet whenever an entity takes damage. Island visitors are
// immune to all damage, as on Hypixel since patch 0.6, and so are players Master Tao protects in the Dojo.
public class ActionAnimateEntityDamage implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityDamageEvent event) {
        if (event.getEntity() instanceof SkyBlockPlayer player
                && (VisitManager.isVisiting(player.getUuid()) || DojoDamageProtection.isProtected(player)
                || HollowsSpawnProtection.isProtected(player))) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof SkyBlockPlayer veiled && CreeperVeil.absorb(veiled)) {
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
