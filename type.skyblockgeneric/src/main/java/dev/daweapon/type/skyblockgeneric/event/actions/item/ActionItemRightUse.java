package dev.daweapon.type.skyblockgeneric.event.actions.item;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.garden.tool.FarmingTools;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.recipe.GUIRecipe;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.InteractableComponent;
import dev.daweapon.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Delegates right-click use to an item's interactable component
public class ActionItemRightUse implements HypixelEventClass {
    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        ItemStack itemStack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(itemStack);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (item.hasComponent(InteractableComponent.class)) {
            event.setCancelled(true);
            InteractableComponent interactableComponent = item.getComponent(InteractableComponent.class);
            interactableComponent.onRightClick(player, item);
            return;
        }

        ItemType nextTier = FarmingTools.nextTier(item.getAttributeHandler().getTypeAsString());
        if (nextTier != null && !SkyBlockRecipe.getFromType(nextTier).isEmpty()) {
            event.setCancelled(true);
            player.openView(new GUIRecipe(nextTier));
        }
    }
}
