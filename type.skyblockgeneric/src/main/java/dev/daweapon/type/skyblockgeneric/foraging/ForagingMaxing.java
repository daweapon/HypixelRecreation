package dev.daweapon.type.skyblockgeneric.foraging;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconRules;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.moonglade.flames.BlueFlamesTrial;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ForagingMaxing {
    private ForagingMaxing() {
    }

    public static void maxOut(SkyBlockPlayer player) {
        BlueFlamesTrial.maxOut(player);
        ChapterTriggers.atLeast(player, MoongladeBeacon.LEVEL_TRIGGER, BeaconRules.MAX_LEVEL);
        MoongladeBeacon.syncSkyBlockXp(player);
    }
}
