package net.swofty.type.skyblockgeneric.accessory.handler;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.type.skyblockgeneric.accessory.OwnedAccessory;
import net.swofty.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Whether a counted accessory works for the player: it must be owned (see {@link AccessoryOwnership})
 * and its item requirements, such as a slayer level, must be met.
 */
public final class UsableAccessory {
    private UsableAccessory() {
    }

    public static boolean usable(SkyBlockPlayer player, ItemType type) {
        for (OwnedAccessory owned : AccessoryOwnership.active(player)) {
            if (owned.type() != type) continue;
            if (!owned.definition().getRealm().isActive()) return false;
            if (!owned.item().hasComponent(ItemRequirementsComponent.class)) return true;
            return owned.item().getComponent(ItemRequirementsComponent.class).canUse(player);
        }
        return false;
    }
}
