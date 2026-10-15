package dev.daweapon.type.kuudra.fight.world;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import org.jetbrains.annotations.Nullable;

/**
 * The run-only items, kept out of ItemType: Elle's tools and the route items bought in the Perk Shop. They carry a
 * tag so they can be recognised and are taken away when the player leaves the run, and are marked uneditable so
 * SkyBlock's item updater leaves them alone instead of rebuilding them as the plain vanilla block.
 */
public final class KuudraItems {
    public static final Tag<String> TAG = Tag.String("kuudra_item");
    private static final String MODELS = "hypixel_skyblock:item/island_relevant/crimson_isle/kuudra/";

    public static final int CARRY_SLOT = 8;

    public enum Kind {ELLES_LAVA_ROD, ELLES_PICKAXE, MINIATURE_NUKE, REMOTE_PERK_SHOP, ELLES_SUPPLIES, FUEL_CELL}

    private KuudraItems() {
    }

    public static ItemStack create(Kind kind) {
        ItemStack.Builder builder = switch (kind) {
            case ELLES_LAVA_ROD -> ItemStacks.item(Material.FISHING_ROD, """
                    <6>Elle's Lava Rod
                    <7>Pulls <c>Elle's supplies <7>and <c>Ballista
                    <c>Fuel Cells <7>out of the lava.

                    <8>Kuudra item: lost when you leave.""")
                    .set(DataComponents.ITEM_MODEL, MODELS + "elles_lava_rod");
            case ELLES_PICKAXE -> ItemStacks.item(Material.IRON_PICKAXE, """
                    <6>Elle's Pickaxe
                    <7>For mining <5>Kuudra's pods<7>.

                    <8>Kuudra item: lost when you leave.""")
                    .set(DataComponents.ITEM_MODEL, MODELS + "elles_pickaxe");
            case MINIATURE_NUKE -> ItemStacks.item(Material.TNT, """
                    <c>Miniature Nuke
                    <7>Deals <c>80% <7>of their Max Health as
                    <7>true damage to every enemy within
                    <a>20 <7>blocks.

                    <e>Right-click to detonate!""");
            case ELLES_SUPPLIES -> ItemStacks.item(Material.CHEST, """
                    <a>Elle's Supplies
                    <8>SUPPLIES
                    <7>Take these supplies back to the
                    <7>middle island for Elle!

                    <f><l>COMMON""");
            case FUEL_CELL -> ItemStacks.item(Material.REDSTONE_LAMP, """
                    <a>Ballista Fuel Cell
                    <8>FUEL
                    <7>Load it into the <c>Ballista <7>in the
                    <7>middle of the platform!

                    <f><l>COMMON""");
            case REMOTE_PERK_SHOP -> ItemStacks.item(Material.ENDER_CHEST, """
                    <a>Remote Perk Shop
                    <7>Open the <5>Perk Shop <7>from anywhere.

                    <e>Right-click to open!""");
        };
        return ItemStacks.notEditable(builder).set(TAG, kind.name()).build();
    }

    public static @Nullable Kind kindOf(ItemStack stack) {
        if (stack == null || stack.isAir()) return null;
        String tag = stack.getTag(TAG);
        if (tag == null) return null;
        try {
            return Kind.valueOf(tag);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static void give(Player player, Kind kind) {
        if (!player.getInventory().addItemStack(create(kind))) {
            player.sendMessage(Text.of("<c>Your inventory is full!").asComponent());
        }
    }

    public static boolean consumeHeld(Player player, Kind kind) {
        ItemStack held = player.getItemInMainHand();
        if (kindOf(held) != kind) return false;
        player.setItemInMainHand(held.amount() > 1 ? held.withAmount(held.amount() - 1) : ItemStack.AIR);
        return true;
    }

    /** Takes every Kuudra item out of the player's inventory. */
    public static void strip(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (kindOf(player.getInventory().getItemStack(slot)) != null) {
                player.getInventory().setItemStack(slot, ItemStack.AIR);
            }
        }
    }
}
