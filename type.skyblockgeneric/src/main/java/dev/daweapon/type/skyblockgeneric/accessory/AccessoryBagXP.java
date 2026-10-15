package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Accessory Bag SkyBlock XP task: +1 XP per point of Accessory Power the player has now. */
public final class AccessoryBagXP {
    private AccessoryBagXP() {
    }

    public static void sync(SkyBlockPlayer player) {
        if (player.getSkyBlockExperience() == null) return;
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ACCESSORY_BAG, AccessoryPower.of(player));
    }
}
