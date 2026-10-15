package dev.daweapon.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.levels.abstr.CauseEmblem;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
public class SkillLevelCause extends SkyBlockLevelCauseAbstr implements CauseEmblem {
    private final SkillCategories category;
    private final int level;

    public SkillLevelCause(SkillCategories category, int level) {
        this.category = category;
        this.level = level;
    }

    @Override
    public String displayName() {
        return category + " Skill " + level;
    }

    @Override
    public double xpReward() {
        for (SkillCategory.Reward unlock : category.asCategory().getReward(level).unlocks()) {
            if (unlock.type() == SkillCategory.Reward.UnlockType.XP) {
                return ((SkillCategory.XPReward) unlock).getXP();
            }
        }
        return 5;
    }

    @Override
    public String getEmblemRequiresMessage() {
        return "Requires " + category + " Skill " + level;
    }

    @Override
    public String emblemEisplayName() {
        return category.toString() + " Skill " + level;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false; // This is handled by the Skill up message
    }
}
