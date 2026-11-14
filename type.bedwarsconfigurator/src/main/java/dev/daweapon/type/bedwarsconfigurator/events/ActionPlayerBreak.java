package dev.daweapon.type.bedwarsconfigurator.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Prevents block breaking in the configurator world during gameplay phase.
public class ActionPlayerBreak implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER , requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockBreakEvent event) {
        event.setCancelled(true);
    }
}

