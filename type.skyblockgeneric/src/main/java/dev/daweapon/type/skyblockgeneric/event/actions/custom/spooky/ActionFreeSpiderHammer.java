package dev.daweapon.type.skyblockgeneric.event.actions.custom.spooky;

import net.minestom.server.event.player.PlayerStartDiggingEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.FreeSpiders;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionFreeSpiderHammer implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerStartDiggingEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        var position = event.getBlockPosition();
        if (FreeSpiders.hit(player, event.getBlock(), position.blockX(), position.blockY(), position.blockZ())) {
            event.setCancelled(true);
        }
    }
}
