package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockItemAchievements;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.ItemCraftEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
