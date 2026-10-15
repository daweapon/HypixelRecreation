package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Entrance asks for Combat XV, every other floor for a Catacombs level; Master Mode is reserved. */
public final class CatacombsRequirements {
    public static final int ENTRANCE_COMBAT_LEVEL = 15;

    private CatacombsRequirements() {
    }

    public static boolean needsCombat(CatacombsFloor floor) {
        return floor == CatacombsFloor.ENTRANCE;
    }

    public static boolean meets(int combatLevel, int catacombsLevel, CatacombsFloor floor, CatacombsMode mode) {
        if (mode != CatacombsMode.NORMAL) return false;
        if (needsCombat(floor)) return combatLevel >= ENTRANCE_COMBAT_LEVEL;
        return catacombsLevel >= floor.requirement(mode);
    }

    public static boolean meets(SkyBlockPlayer player, CatacombsFloor floor) {
        return meets(combatLevel(player), CatacombsProgress.level(player), floor, CatacombsMode.NORMAL);
    }

    public static int combatLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT);
    }
}
