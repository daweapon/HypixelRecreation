package dev.daweapon.type.generic.event.actions.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.type.AnvilInventory;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.gui.HypixelAnvilGUI;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class ActionPlayerInventoryCloseAnvil implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(InventoryCloseEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        if (HypixelAnvilGUI.anvilGUIs.containsKey(player)) {
            if (event.getInventory() instanceof AnvilInventory) {
                HypixelAnvilGUI.anvilGUIs.get(player).getValue().complete(HypixelAnvilGUI.anvilGUIs.get(player).getKey());
                HypixelAnvilGUI.anvilGUIs.remove(player);
            }
        }
    }
}

