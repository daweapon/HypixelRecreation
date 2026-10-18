package dev.daweapon.type.skyblockgeneric.skill.skills;

import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class RunecraftingSkill {
    private RunecraftingSkill() {
    }

    public static double rankMultiplier(Rank rank) {
        return switch (rank) {
            case STAFF, YOUTUBE, MVP_PLUS_PLUS -> 3D;
            case MVP_PLUS -> 2D;
            case MVP -> 1.5D;
            case VIP_PLUS -> 1.25D;
            case VIP -> 1.1D;
            case DEFAULT -> 1D;
        };
    }

    public static Integer getUnlockedRune(SkyBlockPlayer player) {
        int level = player.getSkills().getCurrentLevel(SkillCategories.RUNECRAFTING);
        if (level == 0) {
            return 0;
        }
        SkillCategory.SkillReward reward = SkillCategories.RUNECRAFTING.asCategory().getReward(level);
        for (SkillCategory.Reward unlock : reward.unlocks()) {
            if (unlock instanceof SkillCategory.RuneReward) {
                return ((SkillCategory.RuneReward) unlock).getRuneLevel();
            }
        }
        return 0;
    }
}
