package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsZone;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Dungeon accessories count for double Accessory Power inside Dungeons, which on this server is a Catacombs run server. */
public final class DungeonAccessoryHook {
    private DungeonAccessoryHook() {
    }

    public static boolean isInDungeon(SkyBlockPlayer player) {
        return CatacombsZone.isInCatacombs();
    }
}
