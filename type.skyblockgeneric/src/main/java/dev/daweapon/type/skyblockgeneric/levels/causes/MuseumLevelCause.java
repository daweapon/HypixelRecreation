package dev.daweapon.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
