package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;

/** Whether a player is a dead Catacombs ghost right now; the Catacombs server plugs its run rules in here. */
public final class DungeonGhosts {
    private static volatile Predicate<SkyBlockPlayer> ghost = _ -> false;

    private DungeonGhosts() {
    }

    public static void use(Predicate<SkyBlockPlayer> source) {
        ghost = source;
    }

    public static boolean isGhost(SkyBlockPlayer player) {
        return ghost.test(player);
    }
}
