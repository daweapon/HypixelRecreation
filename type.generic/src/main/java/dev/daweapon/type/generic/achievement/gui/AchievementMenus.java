package dev.daweapon.type.generic.achievement.gui;

import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.function.Consumer;

public final class AchievementMenus {
    private static volatile Consumer<HypixelPlayer> opener = player -> new GUIAchievementsMenu(null).open(player);

    private AchievementMenus() {
    }

    public static void setOpener(Consumer<HypixelPlayer> opener) {
        AchievementMenus.opener = opener;
    }

    public static void open(HypixelPlayer player) {
        opener.accept(player);
    }
}
