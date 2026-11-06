package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

/** The one run-only item: the ready-up dye in the hotbar before the run starts. */
public final class CatacombsItems {
    public static final int READY_SLOT = 4;
    private static final int MENU_SLOT = 8;
    private static final int INVENTORY_SLOTS = 36;
    private static final Tag<String> TAG = Tag.String("catacombs_item");
    private static final String READY = "READY";

    private CatacombsItems() {
    }

    public static ItemStack ready(boolean ready) {
        ItemStack.Builder builder = ready
                ? ItemStacks.item(Material.LIME_DYE, """
                        <a>You are ready
                        <7>The run starts once everyone is ready.

                        <e>Right-click to unready!""")
                : ItemStacks.item(Material.GRAY_DYE, """
                        <c>Not ready
                        <7>The run starts once everyone is ready.

                        <e>Right-click to ready up!""");
        return ItemStacks.notEditable(builder).set(TAG, READY).build();
    }

    public static boolean isReadyItem(ItemStack stack) {
        return stack != null && !stack.isAir() && READY.equals(stack.getTag(TAG));
    }

    public static void showReady(Player player, boolean ready) {
        showReady(player.getInventory(), ready);
    }

    /** Never covers one of the player's own items: the dye goes to its slot only when that slot is free. */
    static void showReady(PlayerInventory inventory, boolean ready) {
        int slot = readySlot(inventory);
        if (slot >= 0) inventory.setItemStack(slot, ready(ready));
    }

    private static int readySlot(PlayerInventory inventory) {
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            if (isReadyItem(inventory.getItemStack(slot))) return slot;
        }
        if (inventory.getItemStack(READY_SLOT).isAir()) return READY_SLOT;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            if (slot != MENU_SLOT && inventory.getItemStack(slot).isAir()) return slot;
        }
        return -1;
    }

    public static void remove(Player player) {
        remove(player.getInventory());
    }

    static void remove(PlayerInventory inventory) {
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            if (isReadyItem(inventory.getItemStack(slot))) inventory.setItemStack(slot, ItemStack.AIR);
        }
    }
}
