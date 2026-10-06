package net.swofty.type.skyblockgeneric.levels.abstr;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class SkyBlockLevelCauseAbstr {
    public abstract double xpReward();
    public abstract boolean shouldDisplayMessage(SkyBlockPlayer player);

    /** Human-readable label for this task, used by the SkyBlock XP source screens. */
    public String displayName() {
        return StringUtility.toNormalCase(getClass().getSimpleName().replace("LevelCause", ""));
    }
}
