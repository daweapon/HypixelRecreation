package net.swofty.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.museum.MuseumEntry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
public class MuseumLevelCause extends SkyBlockLevelCauseAbstr {
    private final MuseumEntry entry;

    public MuseumLevelCause(MuseumEntry entry) {
        this.entry = entry;
    }

    @Override
    public String displayName() {
        return "Museum Donation: " + entry.name();
    }

    @Override
    public double xpReward() {
        return entry.xp();
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }
}
