package dev.daweapon.type.skyblockgeneric.event.actions.custom.collection;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;

// Grants SkyBlock XP for each collection reward tier a player crosses
public class ActionCollectionHypixelLevel implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CollectionUpdateEvent event) {
        if (CollectionCategories.getCategory(event.getItemType()) == null) return;

        CollectionCategory.ItemCollection collection = CollectionCategories.getCategory(event.getItemType()).getCollection(event.getItemType());
        int newValue = event.getPlayer().getCollection().get(event.getItemType());
        for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
            if (event.getOldValue() < reward.requirement() && newValue >= reward.requirement()) {
                event.getPlayer().getSkyBlockExperience().addExperience(
                        SkyBlockLevelCause.getCollectionCause(event.getItemType(), collection.getPlacementOf(reward))
                );
            }
        }
    }
}
