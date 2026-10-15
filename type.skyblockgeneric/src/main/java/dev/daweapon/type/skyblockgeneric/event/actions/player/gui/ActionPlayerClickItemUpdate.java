package dev.daweapon.type.skyblockgeneric.event.actions.player.gui;

import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Refreshes non-SkyBlock items placed into a player's inventory using the item updater
public class ActionPlayerClickItemUpdate implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(InventoryItemChangeEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getInventory().getViewers().stream().toList().getFirst();

        if (!SkyBlockItem.isSkyBlockItem(event.getNewItem())) {
            ItemStack updated = PlayerItemUpdater.playerUpdate(player, event.getNewItem()).build();
            if (!updated.equals(event.getNewItem())) player.getInventory().setItemStack(event.getSlot(), updated);
        }
    }
}

