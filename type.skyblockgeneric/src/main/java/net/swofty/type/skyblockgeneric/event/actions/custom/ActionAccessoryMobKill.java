package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.accessory.handler.KillAccessories;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;

public class ActionAccessoryMobKill implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        KillAccessories.onKill(event.getPlayer(), event.getKilledMob());
    }
}
