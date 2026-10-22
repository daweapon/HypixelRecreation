package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BeaconFuel {
    private BeaconFuel() {
    }

    public static @Nullable ItemType fuelType(ItemStack stack) {
        if (stack == null || stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return null;
        ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
        return BeaconService.isFuel(type) ? type : null;
    }

    public static boolean insertCursor(SkyBlockPlayer player) {
        ItemStack cursor = player.getInventory().getCursorItem();
        ItemType type = fuelType(cursor);
        if (type == null || player.getSkyBlockIsland() == null || BeaconService.get(player.getSkyBlockIsland()) == null) return false;
        player.getInventory().setCursorItem(ItemStack.AIR);
        BeaconService.insertCrystals(player.getSkyBlockIsland(), cursor.amount(), type == ItemType.SCORCHED_POWER_CRYSTAL);
        announce(player, type, cursor.amount());
        return true;
    }

    public static boolean insertFromInventory(SkyBlockPlayer player) {
        if (BeaconService.get(player.getSkyBlockIsland()) == null) return false;
        for (ItemType type : new ItemType[]{ItemType.SCORCHED_POWER_CRYSTAL, ItemType.POWER_CRYSTAL}) {
            if (player.takeItem(type, 1) == null) continue;
            BeaconService.insertCrystals(player.getSkyBlockIsland(), 1, type == ItemType.SCORCHED_POWER_CRYSTAL);
            announce(player, type, 1);
            return true;
        }
        return false;
    }

    public static boolean insertHeld(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        ItemType type = fuelType(held);
        if (type == null || BeaconService.get(player.getSkyBlockIsland()) == null) return false;
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
        BeaconService.insertCrystals(player.getSkyBlockIsland(), 1, type == ItemType.SCORCHED_POWER_CRYSTAL);
        announce(player, type, 1);
        return true;
    }

    private static void announce(SkyBlockPlayer player, ItemType type, int amount) {
        BeaconData data = BeaconService.get(player.getSkyBlockIsland());
        long remaining = data == null ? 0 : data.remainingMillis(System.currentTimeMillis());
        player.sendMessage("<a>You added " + amount + " " + type.getDisplayName() + "<a> to your Beacon! It is now powered for <e>"
                + BeaconService.formatRemaining(remaining) + "<a>.");
    }
}
