package dev.daweapon.type.skyblockgeneric.event.actions.player.offhand;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Blocks offhand-hand animation clicks, since SkyBlock does not support offhand items
public class ActionOffhandHandClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerHandAnimationEvent event) {
        if (event.getHand().equals(PlayerHand.OFF)) {
            ((SkyBlockPlayer) event.getPlayer()).sendMessage("<c>You cannot use your offhand!");
            event.setCancelled(true);
        }
    }
}
