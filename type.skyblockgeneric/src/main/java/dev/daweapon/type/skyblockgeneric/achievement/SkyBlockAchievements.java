package dev.daweapon.type.skyblockgeneric.achievement;

import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.achievement.AchievementCategory;
import dev.daweapon.type.generic.achievement.PlayerAchievementHandler;
import dev.daweapon.type.generic.achievement.gui.AchievementMenus;
import dev.daweapon.type.generic.achievement.gui.AchievementNav;
import dev.daweapon.type.generic.achievement.gui.GUIGameAchievements;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockProfile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class SkyBlockAchievements {
    private SkyBlockAchievements() {
    }

    public static void initialize() {
        AchievementMenus.setOpener(SkyBlockAchievements::open);
        PlayerAchievementHandler.setCoopResolver(SkyBlockAchievements::coopMembers);
    }

    public static void open(HypixelPlayer player) {
        AchievementNav nav = new AchievementNav("Your Equipment and Stats",
                back -> ((SkyBlockPlayer) back).openView(new GUISkyBlockProfile()));
        new GUIGameAchievements(AchievementCategory.SKYBLOCK, nav).open(player);
    }

    private static List<HypixelPlayer> coopMembers(HypixelPlayer player) {
        if (!(player instanceof SkyBlockPlayer skyBlockPlayer)) return List.of();
        CoopDatabase.Coop coop = skyBlockPlayer.getCoop();
        if (coop == null) return List.of();
        return HypixelGenericLoader.getLoadedPlayers().stream()
                .filter(other -> coop.members().contains(other.getUuid()))
                .toList();
    }
}
