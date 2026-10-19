package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import org.jetbrains.annotations.Nullable;

public enum MiningEventZone {
    DWARVEN_MINES("the Dwarven Mines", ServerType.SKYBLOCK_DWARVEN_MINES),
    CRYSTAL_HOLLOWS("the Crystal Hollows", ServerType.SKYBLOCK_CRYSTAL_HOLLOWS);

    private final String phrase;
    private final ServerType serverType;

    MiningEventZone(String phrase, ServerType serverType) {
        this.phrase = phrase;
        this.serverType = serverType;
    }

    public String phrase() {
        return phrase;
    }

    public static @Nullable MiningEventZone ofServer() {
        ServerType type = HypixelConst.getTypeLoader().getType();
        for (MiningEventZone zone : values()) {
            if (zone.serverType == type) return zone;
        }
        return null;
    }
}
