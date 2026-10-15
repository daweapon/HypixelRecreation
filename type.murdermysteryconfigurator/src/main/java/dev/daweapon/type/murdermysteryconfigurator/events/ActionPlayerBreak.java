package dev.daweapon.type.murdermysteryconfigurator.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPlayerBreak implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockBreakEvent event) {
        // Prevent block breaking in configurator mode
        event.setCancelled(true);
    }
}
