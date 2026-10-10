package net.swofty.type.skyblockgeneric.achievement;

import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.achievement.gui.AchievementMenus;
import net.swofty.type.generic.achievement.gui.AchievementNav;
import net.swofty.type.generic.achievement.gui.GUIGameAchievements;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockProfile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
