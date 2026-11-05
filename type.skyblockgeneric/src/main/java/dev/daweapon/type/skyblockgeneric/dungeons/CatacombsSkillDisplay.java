package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lore lines for the Catacombs entry of the Dungeoneering skill: progress to the next level and each level's rewards. */
public final class CatacombsSkillDisplay {
    public static final int BAR_LENGTH = 20;

    private CatacombsSkillDisplay() {
    }

    public static List<String> progressLines(long xp, String prefix) {
        int level = CatacombsLevels.levelForXp(xp);
        if (level >= CatacombsLevels.MAXIMUM_LEVEL) return List.of("<6><l>MAX LEVEL REACHED");
        long into = xp - CatacombsLevels.xpForLevel(level);
        long needed = CatacombsLevels.xpToReachLevel(level + 1);
        double fraction = Math.max(0, Math.min(1, (double) into / needed));
        int filled = (int) Math.floor(fraction * BAR_LENGTH);
        return List.of(
                prefix + "<e>" + percent(fraction * 100) + "%",
                Text.of("<2><m>{}<f><m>{}<r> <e>{:,}<6>/<e>{:short}",
                        "-".repeat(filled), "-".repeat(BAR_LENGTH - filled), into, needed).serialize());
    }

    public static List<String> nextLevelProgress(long xp) {
        int next = CatacombsLevels.levelForXp(xp) + 1;
        return progressLines(xp, Text.of("<7>Progress to Level {:roman}: ", next).serialize());
    }

    public static List<String> rewardLines(int level) {
        List<String> lore = new ArrayList<>();
        lore.add("<7>Rewards:");
        lore.add("<7>  <8>+<a>" + CatacombsLevels.HEALTH_PER_LEVEL + " <c>❤ Health");
        lore.add("<7>  Increase the base stats of your");
        lore.add("<7>  dungeon items by <c>" + CatacombsLevels.itemStatBonusIncrease(level) + "% <7>while in");
        lore.add("<7>  <c>The Catacombs<7>.");
        for (CatacombsMode mode : CatacombsMode.values()) {
            for (CatacombsFloor floor : CatacombsFloor.values()) {
                if (floor.supports(mode) && floor != CatacombsFloor.ENTRANCE && floor.requirement(mode) == level) {
                    lore.add("<7>  <8>+<a>Access to <c>" + (mode == CatacombsMode.MASTER ? "Master Mode " : "") + floor.displayName());
                }
            }
        }
        lore.add("<7>  <8>+<b>" + CatacombsSkyBlockXp.levelUpXp(level) + " SkyBlock XP");
        return lore;
    }

    private static String percent(double value) {
        String formatted = String.format(Locale.ENGLISH, "%.1f", value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }
}
