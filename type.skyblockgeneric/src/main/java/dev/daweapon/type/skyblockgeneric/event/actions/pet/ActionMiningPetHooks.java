package dev.daweapon.type.skyblockgeneric.event.actions.pet;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.MiningOffCamera;

// Precursor Drone's Mining Off Camera: mining collection progress can drop an enchanted mining item
public class ActionMiningPetHooks implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCollection(CollectionUpdateEvent event) {
        int gained = event.getPlayer().getCollection().get(event.getItemType()) - event.getOldValue();
        MiningOffCamera.collectionProgressed(event.getPlayer(), event.getItemType(), gained);
    }
}
