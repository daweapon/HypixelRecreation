package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;

import java.util.function.BooleanSupplier;

/** Whether this server is a Catacombs run server, where dungeon-only bonuses (item scaling, Catacombs Health) apply. */
public final class CatacombsZone {
    private static volatile BooleanSupplier inCatacombs = CatacombsZone::isCatacombsServer;

    private CatacombsZone() {
    }

    public static boolean isInCatacombs() {
        return inCatacombs.getAsBoolean();
    }

    public static void use(BooleanSupplier source) {
        inCatacombs = source;
    }

    public static void useServerType() {
        inCatacombs = CatacombsZone::isCatacombsServer;
    }

    private static boolean isCatacombsServer() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_CATACOMBS;
    }
}
