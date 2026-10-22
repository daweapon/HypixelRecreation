package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.torrhus.DesertTemple;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;

public class ActionDesertTemple implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        if (DesertTemple.handle((SkyBlockPlayer) event.getPlayer(), event.getBlockPosition(), event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
