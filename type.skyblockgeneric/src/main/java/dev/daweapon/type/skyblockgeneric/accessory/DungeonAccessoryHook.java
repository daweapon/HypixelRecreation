package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Dungeon accessories count for double Accessory Power inside Dungeons. Dungeons aren't built yet, so
 * this is the one switch to flip when a run can tell the player is inside one.
 */
public final class DungeonAccessoryHook {
    public static final String UNAVAILABLE_REASON = "Dungeons aren't available yet.";

    private DungeonAccessoryHook() {
    }

    public static boolean isInDungeon(SkyBlockPlayer player) {
        return false;
    }
}
