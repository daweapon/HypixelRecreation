package net.swofty.type.skyblockgeneric.accessory;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.NewAccessoryLevelCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
