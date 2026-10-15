package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * A blacksmith menu with one item slot (13) and an action button (22), like the Reforge menu.
 */
abstract class EssenceItemSlotGUI extends HypixelInventoryGUI {
    protected static final int ITEM_SLOT = 13;
    protected static final int ACTION_SLOT = 22;
    private static final int[] BORDER = {0, 8, 9, 17, 18, 26, 27, 35, 36, 44};

    protected EssenceItemSlotGUI(String title) {
        super(Text.literal(title), InventoryType.CHEST_5_ROW);
    }

    protected abstract ItemStack.Builder actionButton(SkyBlockPlayer player, @Nullable SkyBlockItem item);

    protected abstract boolean accepts(SkyBlockItem item);

    /** Runs the action; returns the item to keep in the slot, or null when it was consumed. */
    protected abstract @Nullable SkyBlockItem act(SkyBlockPlayer player, SkyBlockItem item);

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getCloseItem(40));
        show(null);
    }

    protected void show(@Nullable SkyBlockItem item) {
        boolean valid = item != null && accepts(item);
        for (int slot : BORDER) {
            set(slot, ItemStacks.named(valid ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, ""));
        }
        set(new GUIClickableItem(ITEM_SLOT) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack cursor = player.getInventory().getCursorItem();
                e.setCancelled(true);
                if (item == null) {
                    if (cursor.isAir() || cursor.get(DataComponents.CUSTOM_NAME) == null) return;
                    player.getInventory().setCursorItem(ItemStack.AIR);
                    show(new SkyBlockItem(cursor));
                    return;
                }
                if (!cursor.isAir()) return;
                player.getInventory().setCursorItem(PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
                show(null);
            }

            @Override
            public boolean canPickup() {
                return true;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return item == null ? ItemStack.builder(Material.AIR)
                        : PlayerItemUpdater.playerUpdate((SkyBlockPlayer) p, item.getItemStack());
            }
        });
        set(new GUIClickableItem(ACTION_SLOT) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (item == null || !accepts(item)) return;
                show(act((SkyBlockPlayer) p, item));
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return actionButton((SkyBlockPlayer) p, item);
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        returnItem((SkyBlockPlayer) e.getPlayer(), e.getInventory());
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        returnItem((SkyBlockPlayer) player, inventory);
    }

    private static void returnItem(SkyBlockPlayer player, AbstractInventory inventory) {
        ItemStack stack = inventory.getItemStack(ITEM_SLOT);
        if (stack.isAir()) return;
        player.addAndUpdateItem(new SkyBlockItem(stack));
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
