package net.swofty.type.skyblockgeneric.event.actions.pet;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.MiningOffCamera;

// Precursor Drone's Mining Off Camera: mining collection progress can drop an enchanted mining item
public class ActionMiningPetHooks implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCollection(CollectionUpdateEvent event) {
        int gained = event.getPlayer().getCollection().get(event.getItemType()) - event.getOldValue();
        MiningOffCamera.collectionProgressed(event.getPlayer(), event.getItemType(), gained);
    }
}
