package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class AbiphoneContactDirectory {
    public static final int TOTAL = AbiphoneContactXP.MAX_CONTACTS;

    private AbiphoneContactDirectory() {
    }

    public static int discovered(SkyBlockPlayer player) {
        return Math.min(AbiphoneContactXP.unlocked(player).size(), TOTAL);
    }
}
