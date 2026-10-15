package dev.daweapon.type.ravengarddungeon.events;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengarddungeon.interactables.InteractableRegistry;

// Forwards player interactions with dungeon interactable entities (and inventory closes) to the InteractableRegistry.
public class ActionPlayerInteractables implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        org.tinylog.Logger.info("Interact click: entity {} type {} resolved {}",
                event.getTarget().getEntityId(), event.getTarget().getEntityType(),
                InteractableRegistry.byInteraction(event.getTarget()) != null);
        InteractableRegistry.onClick(event.getPlayer(), event.getTarget());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() instanceof net.minestom.server.inventory.Inventory inventory) {
            InteractableRegistry.onInventoryClose(event.getPlayer(), inventory);
        }
    }
}
