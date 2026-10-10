package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.achievement.SkyBlockItemAchievements;
import net.swofty.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import net.swofty.type.skyblockgeneric.event.custom.ItemCraftEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionItemAchievements implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCraft(ItemCraftEvent event) {
        SkyBlockItem crafted = event.getCraftedItem();
        boolean minion = crafted.hasComponent(MinionComponent.class);
        int tier = minion ? crafted.getAttributeHandler().getMinionData().tier() : 0;
        SkyBlockItemAchievements.onCrafted(event.getPlayer(), crafted.getAttributeHandler().getPotentialType(),
                minion, tier, event.isNewMinionSlotUnlocked());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCollection(CollectionUpdateEvent event) {
        SkyBlockItemAchievements.onGreaterAccessoryBagCheck(event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SkyBlockItemAchievements.onGreaterAccessoryBagCheck((SkyBlockPlayer) event.getPlayer());
    }
}
