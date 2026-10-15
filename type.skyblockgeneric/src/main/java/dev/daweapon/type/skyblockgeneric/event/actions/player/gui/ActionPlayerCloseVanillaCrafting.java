package dev.daweapon.type.skyblockgeneric.event.actions.player.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.PlayerInventory;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.PlayerInventoryCrafting;

// Returns items from the vanilla crafting grid to the player's inventory when they close it
public class ActionPlayerCloseVanillaCrafting implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void run(InventoryCloseEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory)) {
            return;
        }

        PlayerInventoryCrafting.returnCraftingGrid((SkyBlockPlayer) event.getPlayer());
    }
}
