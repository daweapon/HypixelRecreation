package net.swofty.type.generic.achievement.gui;

import net.swofty.type.generic.user.HypixelPlayer;

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
