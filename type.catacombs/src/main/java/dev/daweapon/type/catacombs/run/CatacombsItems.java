package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

/** The one run-only item: the ready-up dye in the hotbar before the run starts. */
public final class CatacombsItems {
    public static final int READY_SLOT = 4;
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
        player.getInventory().setItemStack(READY_SLOT, ready(ready));
    }

    public static void remove(Player player) {
        if (isReadyItem(player.getInventory().getItemStack(READY_SLOT))) {
            player.getInventory().setItemStack(READY_SLOT, ItemStack.AIR);
        }
    }
}
