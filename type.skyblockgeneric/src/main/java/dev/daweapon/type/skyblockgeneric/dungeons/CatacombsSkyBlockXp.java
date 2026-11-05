package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The two Catacombs SkyBlock XP tasks from the wiki's Dungeon Tasks table: Catacombs Level Up pays 20 XP for
 * levels 1-39 and 40 XP for levels 40-50 (1,220), and Complete Dungeons pays once per floor: 20 for Entrance to
 * Floor IV, 30 for Floors V-VII and 50 for each Master Mode floor (540).
 */
public final class CatacombsSkyBlockXp {
    public static final int LATE_LEVEL = 40;

    private CatacombsSkyBlockXp() {
    }

    public static int levelUpXp(int level) {
        if (level < 1 || level > CatacombsLevels.MAXIMUM_LEVEL) return 0;
        return level < LATE_LEVEL ? 20 : 40;
    }

    public static int levelUpTotal(int level) {
        int total = 0;
        for (int reached = 1; reached <= Math.min(level, CatacombsLevels.MAXIMUM_LEVEL); reached++) total += levelUpXp(reached);
        return total;
    }

    public static int floorXp(CatacombsFloor floor, CatacombsMode mode) {
        if (!floor.supports(mode)) return 0;
        if (mode == CatacombsMode.MASTER) return 50;
        return floor.ordinal() >= CatacombsFloor.FLOOR_FIVE.ordinal() ? 30 : 20;
    }

    public static int completionTotal(CatacombsCompletions completions) {
        int total = 0;
        for (CatacombsMode mode : CatacombsMode.values()) {
            for (CatacombsFloor floor : CatacombsFloor.values()) {
                if (floor.supports(mode) && completions.count(floor, mode) > 0) total += floorXp(floor, mode);
            }
        }
        return total;
    }

    public static void sync(SkyBlockPlayer player, int catacombsLevel, CatacombsCompletions completions) {
        sync(player.getSkyBlockExperience(), catacombsLevel, completions);
    }

    public static void sync(DatapointSkyBlockExperience.PlayerSkyBlockExperience experience, int catacombsLevel,
                            CatacombsCompletions completions) {
        experience.setSourceXP(SkyBlockXPSource.CATACOMBS_LEVEL_UP, levelUpTotal(catacombsLevel));
        experience.setSourceXP(SkyBlockXPSource.COMPLETE_DUNGEONS, completionTotal(completions));
    }
}
