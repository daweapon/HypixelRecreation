package dev.daweapon.type.skyblockgeneric.levels.causes;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.levels.abstr.CauseEmblem;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.Getter;

@Getter
public class VargulMilestoneCause extends SkyBlockLevelCauseAbstr implements CauseEmblem {
    private final int milestone;

    public VargulMilestoneCause(int milestone) {
        this.milestone = milestone;
    }

    private String name() {
        return "Vargul the Unearthed Milestone " + StringUtility.getAsRomanNumeral(milestone);
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
