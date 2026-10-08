package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
