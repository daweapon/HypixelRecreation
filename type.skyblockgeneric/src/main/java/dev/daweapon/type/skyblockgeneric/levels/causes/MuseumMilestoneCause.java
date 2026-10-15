package dev.daweapon.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
public class MuseumMilestoneCause extends SkyBlockLevelCauseAbstr {
    private final int milestone;

    public MuseumMilestoneCause(int milestone) {
        this.milestone = milestone;
    }

    @Override
    public String displayName() {
        return "Museum Milestone " + milestone;
    }

    @Override
    public double xpReward() {
        return 0;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }
}
