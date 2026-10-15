package dev.daweapon.type.skyblockgeneric.crimson.dojo;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Predicate;

/** Lets the Crimson Isle server tell the generic damage handler who Master Tao is protecting. */
public final class DojoDamageProtection {
    private static volatile Predicate<SkyBlockPlayer> check = player -> false;

    private DojoDamageProtection() {
    }

    public static void register(Predicate<SkyBlockPlayer> protectedPlayers) {
        check = protectedPlayers;
    }

    public static boolean isProtected(SkyBlockPlayer player) {
        return check.test(player);
    }
}
