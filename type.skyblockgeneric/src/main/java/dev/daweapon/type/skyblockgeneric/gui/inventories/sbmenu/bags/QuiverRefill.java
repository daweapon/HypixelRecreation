package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// A merchant's "Fill your Quiver" button: tops every unlocked quiver slot up to a full stack of one
// arrow type, empty slots included, at a fixed price per arrow.
public final class QuiverRefill {
    private static final int STACK = 64;

    private QuiverRefill() {
    }

    public static int missingArrows(SkyBlockPlayer player, ItemType arrow) {
        if (!player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return 0;
        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        int missing = 0;
        for (int slot = 0; slot < GUIQuiver.getUnlockedSlots(player); slot++) {
            SkyBlockItem item = quiver.getInSlot(slot);
            if (isEmpty(item)) missing += STACK;
            else if (item.getAttributeHandler().getPotentialType() == arrow) missing += STACK - item.getAmount();
        }
        return missing;
    }

    public static void fill(SkyBlockPlayer player, ItemType arrow, double coinsPerArrow) {
        if (!player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) {
            player.sendMessage("<c>You don't have a Quiver yet!");
            return;
        }
        int missing = missingArrows(player, arrow);
        if (missing <= 0) {
            player.sendMessage("<c>Your Quiver is already full!");
            return;
        }
        double cost = missing * coinsPerArrow;
        if (player.getCoins() < cost) {
            player.sendMessage("<c>You don't have enough coins!");
            return;
        }
        player.removeCoins(cost);

        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        for (int slot = 0; slot < GUIQuiver.getUnlockedSlots(player); slot++) {
            SkyBlockItem item = quiver.getInSlot(slot);
            if (isEmpty(item)) {
                quiver.getQuiverMap().put(slot, new SkyBlockItem(arrow, STACK));
            } else if (item.getAttributeHandler().getPotentialType() == arrow) {
                item.setAmount(STACK);
                quiver.getQuiverMap().put(slot, item);
            }
        }
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class).setValue(quiver);
        player.sendMessage("<a>Filled your Quiver with <f>{} {} <a>for <6>{} coins<a>!",
                StringUtility.commaify(missing), new SkyBlockItem(arrow).getDisplayName(), StringUtility.commaify((long) cost));
    }

    private static boolean isEmpty(SkyBlockItem item) {
        return item == null || item.isNA() || item.getAmount() == 0 || item.getMaterial() == Material.AIR;
    }
}
