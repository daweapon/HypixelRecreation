package net.swofty.type.skyblockgeneric.accessory;

import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Accessory Bag SkyBlock XP task: +1 XP per point of Accessory Power the player has now. */
public final class AccessoryBagXP {
    private AccessoryBagXP() {
    }

    public static void sync(SkyBlockPlayer player) {
        if (player.getSkyBlockExperience() == null) return;
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ACCESSORY_BAG, AccessoryPower.of(player));
    }
}
