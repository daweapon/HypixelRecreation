package dev.daweapon.type.spidersden.events;

import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.EntityRelic;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenRelics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionRelicInteract implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        if (event.getTarget() instanceof EntityRelic relic) SpiderDenRelics.collect((SkyBlockPlayer) event.getPlayer(), relic.relic());
    }
}
