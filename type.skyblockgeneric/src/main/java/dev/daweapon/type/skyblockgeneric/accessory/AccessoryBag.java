package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.causes.NewAccessoryLevelCause;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Saving the Accessory Bag and everything that follows from its contents changing. */
public final class AccessoryBag {
    private AccessoryBag() {
    }

    public static void save(SkyBlockPlayer player, DatapointAccessoryBag.PlayerAccessoryBag bag) {
        for (SkyBlockItem item : bag.getAllAccessories()) discover(player, bag, item.getAttributeHandler().getPotentialType());
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACCESSORY_BAG, DatapointAccessoryBag.class).setValue(bag);
        AccessoryOwnership.invalidate(player);
        AccessoryBagXP.sync(player);
    }

    private static void discover(SkyBlockPlayer player, DatapointAccessoryBag.PlayerAccessoryBag bag, ItemType type) {
        if (type == null || bag.hasDiscoveredAccessory(type)) return;
        bag.addDiscoveredAccessory(type);
        NewAccessoryLevelCause cause = SkyBlockLevelCause.getAccessoryCause(type);
        if (cause != null) player.getSkyBlockExperience().addExperience(cause);
    }
}
