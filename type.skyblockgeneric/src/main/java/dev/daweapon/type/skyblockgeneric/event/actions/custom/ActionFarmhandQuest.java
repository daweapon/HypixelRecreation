package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.farmhand.FarmhandQuest;

public class ActionFarmhandQuest implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCollection(CollectionUpdateEvent event) {
        FarmhandQuest.sync(event.getPlayer());
    }
}
