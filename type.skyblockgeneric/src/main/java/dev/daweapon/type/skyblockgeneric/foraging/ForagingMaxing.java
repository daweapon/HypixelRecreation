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
        for (dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconSite site : dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconSite.values())
            ChapterTriggers.atLeast(player, site.levelTrigger(), BeaconRules.MAX_LEVEL);
        MoongladeBeacon.syncSkyBlockXp(player);
    }
}
