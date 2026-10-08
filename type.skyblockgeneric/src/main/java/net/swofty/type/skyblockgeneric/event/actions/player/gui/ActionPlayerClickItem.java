package net.swofty.type.skyblockgeneric.event.actions.player.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoChallengeLock;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockMenu;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Keeps the SkyBlock menu item locked in the last hotbar slot and opens the menu when it is clicked
public class ActionPlayerClickItem implements HypixelEventClass {
    public static final int MENU_SLOT = 8;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(InventoryPreClickEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (DojoChallengeLock.isLocked(player) && touchesMenuSlot(event)) {
            event.setCancelled(true);
            if (event.getClick() instanceof Click.Left || event.getClick() instanceof Click.Right) DojoChallengeLock.cancel(player);
            return;
        }
        if (touchesMenuSlot(event) || isMenu(event.getClickedItem()) || isMenu(player.getInventory().getCursorItem())) {
            event.setCancelled(true);
            if (isMenu(event.getClickedItem()) && (event.getClick() instanceof Click.Left || event.getClick() instanceof Click.Right)) {
                player.openView(new GUISkyBlockMenu());
            }
        }
    }

    private static boolean touchesMenuSlot(InventoryPreClickEvent event) {
        Click click = event.getClick();
        if (click instanceof Click.HotbarSwap swap && swap.hotbarSlot() == MENU_SLOT) return true;
        if (!(event.getInventory() instanceof PlayerInventory)) return false;
        return switch (click) {
            case Click.LeftDrag drag -> drag.slots().contains(MENU_SLOT);
            case Click.RightDrag drag -> drag.slots().contains(MENU_SLOT);
            case Click.MiddleDrag drag -> drag.slots().contains(MENU_SLOT);
            default -> event.getSlot() == MENU_SLOT;
        };
    }

    private static boolean isMenu(ItemStack stack) {
        return SkyBlockItem.isSkyBlockItem(stack) && new SkyBlockItem(stack).getItemType() == ItemType.SKYBLOCK_MENU;
    }
}
