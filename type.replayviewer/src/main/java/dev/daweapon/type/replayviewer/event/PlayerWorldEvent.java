package dev.daweapon.type.replayviewer.event;

import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

public class PlayerWorldEvent implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onBlockBreak(PlayerBlockBreakEvent event) {
        event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void onBlockPlace(PlayerBlockPlaceEvent event) {
        event.setCancelled(true);
    }

}
