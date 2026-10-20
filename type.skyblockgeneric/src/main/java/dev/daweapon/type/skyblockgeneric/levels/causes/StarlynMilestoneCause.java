package dev.daweapon.type.skyblockgeneric.levels.causes;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.levels.abstr.CauseEmblem;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;

@Getter
public class StarlynMilestoneCause extends SkyBlockLevelCauseAbstr implements CauseEmblem {
    private final String sister;
    private final int milestone;

    public StarlynMilestoneCause(String sister, int milestone) {
        this.sister = sister;
        this.milestone = milestone;
    }

    private String name() {
        return StringUtility.toNormalCase(sister) + "'s Milestone " + StringUtility.getAsRomanNumeral(milestone);
    }

    @Override
    public String displayName() {
        return name();
    }

    @Override
    public double xpReward() {
        return 0;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }

    @Override
    public String getEmblemRequiresMessage() {
        return "Reach " + name() + ".";
    }

    @Override
    public String emblemEisplayName() {
        return name();
    }
}
