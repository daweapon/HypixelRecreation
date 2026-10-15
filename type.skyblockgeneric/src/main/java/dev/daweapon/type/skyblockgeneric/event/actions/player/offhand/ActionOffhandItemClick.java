package dev.daweapon.type.skyblockgeneric.event.actions.player.offhand;

import net.minestom.server.event.player.PlayerSwapItemEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Blocks offhand item swapping, since SkyBlock does not support offhand items
public class ActionOffhandItemClick implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSwapItemEvent event) {
        ((SkyBlockPlayer) event.getPlayer()).sendMessage("<c>You cannot use your offhand!");
        event.setCancelled(true);
    }
}
