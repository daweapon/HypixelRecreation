package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AbiphoneCases {
    private AbiphoneCases() {
    }

    public static boolean isCase(String itemType) {
        return itemType != null && itemType.endsWith("_ABICASE");
    }

    public static Set<String> effective(List<String> accessoryBag, List<String> inventory) {
        Set<String> effective = new LinkedHashSet<>();
        accessoryBag.stream().filter(AbiphoneCases::isCase).findFirst().ifPresent(effective::add);
        inventory.stream().filter(AbiphoneCases::isCase).forEach(effective::add);
        return effective;
    }

    public static Set<String> effective(SkyBlockPlayer player) {
        List<String> bag = new ArrayList<>();
        for (SkyBlockItem item : player.getAccessoryBag().getAllAccessories()) bag.add(item.getAttributeHandler().getTypeAsString());
        List<String> inventory = new ArrayList<>();
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            inventory.add(new SkyBlockItem(stack).getAttributeHandler().getTypeAsString());
        }
        return effective(bag, inventory);
    }

    public static int contactSlots(Set<String> effective) {
        if (effective.contains("SUMSUNG_GG_ABICASE")) return 2;
        return effective.contains("SUMSUNG_G3_ABICASE") ? 1 : 0;
    }

    public static boolean singleRing(Set<String> effective) {
        return effective.contains("REZAR_ABICASE");
    }
}
