package dev.daweapon.type.skyblockgeneric.event.actions.player.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.PlayerInventoryCrafting;

import java.util.List;

// Intercepts clicks and drags in the vanilla crafting grid to drive SkyBlock's custom crafting logic
public class ActionPlayerInteractWithCrafting implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(InventoryPreClickEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (!(event.getInventory() instanceof PlayerInventory)) return;

        if (event.getClick() instanceof Click.LeftDrag(List<Integer> slots)) {
            handleDrag(event, player, slots);
            return;
        }

        if (event.getClick() instanceof Click.RightDrag(List<Integer> slots)) {
            handleDrag(event, player, slots);
            return;
        }

        if (PlayerInventoryCrafting.isResultSlot(event.getSlot())) {
            event.setCancelled(true);
            PlayerInventoryCrafting.craft(player, event.getClick());
            return;
        }

        if (PlayerInventoryCrafting.isCraftingSlot(event.getSlot())
                || event.getClick() instanceof Click.LeftShift
                || event.getClick() instanceof Click.RightShift) {
            PlayerInventoryCrafting.refreshNextTick(player);
        }
    }

    private void handleDrag(InventoryPreClickEvent event, SkyBlockPlayer player, Iterable<Integer> slots) {
        boolean refresh = false;
        for (int slot : slots) {
            if (PlayerInventoryCrafting.isResultSlot(slot)) {
                event.setCancelled(true);
                return;
            }
            if (PlayerInventoryCrafting.isCraftingSlot(slot)) {
                refresh = true;
            }
        }

        if (refresh) {
            PlayerInventoryCrafting.refreshNextTick(player);
        }
    }
}
