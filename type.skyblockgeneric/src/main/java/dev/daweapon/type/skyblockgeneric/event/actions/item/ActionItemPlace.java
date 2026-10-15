package dev.daweapon.type.skyblockgeneric.event.actions.item;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PlaceEventComponent;
import dev.daweapon.type.skyblockgeneric.item.components.PlaceableComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Handles placing SkyBlock items as blocks, delegating to a custom place event or placeable component
public class ActionItemPlace implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockPlaceEvent event) {
        ItemStack itemStack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(itemStack);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (item.hasComponent(PlaceEventComponent.class)) {
            PlaceEventComponent placeEvent = item.getComponent(PlaceEventComponent.class);
            placeEvent.handlePlace(event, player, item);
            return;
        }

        if (!item.hasComponent(PlaceableComponent.class)) {
            event.setCancelled(true);
        } else {
            PlaceableComponent placeableItem = item.getComponent(PlaceableComponent.class);
            if (placeableItem.getBlockType() != null) {
                event.setBlock(new SkyBlockBlock(placeableItem.getBlockType()).toBlock());
            }
        }
    }
}
