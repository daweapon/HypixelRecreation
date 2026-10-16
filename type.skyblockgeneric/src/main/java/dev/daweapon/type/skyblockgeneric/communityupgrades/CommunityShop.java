package dev.daweapon.type.skyblockgeneric.communityupgrades;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

public final class CommunityShop {
    private static volatile Consumer<SkyBlockPlayer> opener;

    private CommunityShop() {
    }

    public static void register(Consumer<SkyBlockPlayer> shopOpener) {
        opener = shopOpener;
    }

    public static void open(SkyBlockPlayer player) {
        Consumer<SkyBlockPlayer> current = opener;
        if (current == null) {
            player.sendMessage("<c>Elizabeth can only open the Community Shop while you are in the Hub.");
            return;
        }
        current.accept(player);
    }
}
