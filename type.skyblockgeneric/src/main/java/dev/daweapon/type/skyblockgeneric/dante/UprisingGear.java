package dev.daweapon.type.skyblockgeneric.dante;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class UprisingGear {
    public static final List<ItemType> RESISTANCE_KIT = List.of(
            ItemType.HOPE_OF_THE_RESISTANCE,
            ItemType.ARMOR_OF_THE_RESISTANCE_HELMET, ItemType.ARMOR_OF_THE_RESISTANCE_CHESTPLATE,
            ItemType.ARMOR_OF_THE_RESISTANCE_LEGGINGS, ItemType.ARMOR_OF_THE_RESISTANCE_BOOTS);
    public static final List<ItemType> GENERALS_KIT = List.of(
            ItemType.GENERALS_HOPE_OF_THE_RESISTANCE,
            ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_HELMET, ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_CHESTPLATE,
            ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_LEGGINGS, ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_BOOTS);

    private UprisingGear() {
    }

    public static void give(SkyBlockPlayer player, List<ItemType> kit) {
        for (ItemType item : kit) player.addAndUpdateItem(new SkyBlockItem(item));
    }

    public static void giveIfMissing(SkyBlockPlayer player) {
        boolean armed = player.getAmountInInventory(ItemType.HOPE_OF_THE_RESISTANCE) > 0
                || player.getAmountInInventory(ItemType.GENERALS_HOPE_OF_THE_RESISTANCE) > 0;
        if (armed) return;
        give(player, RESISTANCE_KIT);
        player.sendMessage("<d>[NPC] Clerk Seraphine<f>: Take the <6>Staff <f>and <6>Armor of the Rising Sun<f>. Good luck!");
    }
}
