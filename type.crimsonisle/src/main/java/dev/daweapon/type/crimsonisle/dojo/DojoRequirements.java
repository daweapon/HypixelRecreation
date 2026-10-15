package dev.daweapon.type.crimsonisle.dojo;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.event.actions.player.gui.ActionPlayerClickItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockInventory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/** Master Tao only tests bare skills: no items, no armor, no equipment and no mount. */
public final class DojoRequirements {
    public static final String WITH_ITEMS = "I only test people who use their bare skills. No extra help allowed! Come back to me once you've <e>stored your items away<f>.";
    public static final String WITH_EQUIPMENT = "Your inventory includes your <e>equipment <f>too! Make sure that is cleared!";
    public static final String MOUNTED = "No pets allowed in the dojo! Get off of your pet.";

    private DojoRequirements() {
    }

    /** Master Tao's refusal line, or null when the player may start. */
    public static @Nullable String problem(SkyBlockPlayer player) {
        if (player.getVehicle() != null) return MOUNTED;
        if (hasItems(player)) return WITH_ITEMS;
        if (hasEquipment(player)) return WITH_EQUIPMENT;
        return null;
    }

    public static boolean isEmptyHanded(SkyBlockPlayer player) {
        return !hasItems(player) && !hasEquipment(player);
    }

    public static boolean hasItems(SkyBlockPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == ActionPlayerClickItem.MENU_SLOT) continue;
            if (!player.getInventory().getItemStack(slot).isAir()) return true;
        }
        if (!player.getInventory().getCursorItem().isAir()) return true;
        if (!player.getItemInOffHand().isAir()) return true;
        return List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots())
                .stream().anyMatch(stack -> !stack.isAir());
    }

    public static boolean hasEquipment(SkyBlockPlayer player) {
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        return Arrays.asList(inventory.getNecklace(), inventory.getCloak(), inventory.getBelt(), inventory.getGloves())
                .stream().anyMatch(DojoRequirements::isPresent);
    }

    private static boolean isPresent(@Nullable UnderstandableSkyBlockItem item) {
        return item != null && !new SkyBlockItem(item).isAir();
    }

    public static void clearInventory(SkyBlockPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot != ActionPlayerClickItem.MENU_SLOT) player.getInventory().setItemStack(slot, ItemStack.AIR);
        }
        player.getInventory().setCursorItem(ItemStack.AIR);
        player.setItemInOffHand(ItemStack.AIR);
    }
}
