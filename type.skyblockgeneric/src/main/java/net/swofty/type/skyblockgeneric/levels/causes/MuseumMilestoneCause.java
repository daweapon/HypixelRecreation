package net.swofty.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
