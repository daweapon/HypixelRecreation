package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CrimsonLocation {
    private CrimsonLocation() {
    }

    public static boolean isCrimsonIsleServer() {
        return HypixelConst.getTypeLoader() != null
                && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_CRIMSON_ISLE;
    }

    public static boolean isOnCrimsonIsle(SkyBlockPlayer player) {
        return player != null && isCrimsonIsleServer();
    }
}
