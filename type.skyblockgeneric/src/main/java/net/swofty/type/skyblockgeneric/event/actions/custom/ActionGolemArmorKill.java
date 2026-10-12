package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.set.sets.GolemArmorSet;

public class ActionGolemArmorKill implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        GolemArmorSet.onKill(event.getPlayer());
    }
}
