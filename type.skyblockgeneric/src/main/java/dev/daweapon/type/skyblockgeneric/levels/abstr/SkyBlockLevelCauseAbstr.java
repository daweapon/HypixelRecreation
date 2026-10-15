package dev.daweapon.type.skyblockgeneric.levels.abstr;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public abstract class SkyBlockLevelCauseAbstr {
    public abstract double xpReward();
    public abstract boolean shouldDisplayMessage(SkyBlockPlayer player);

    /** Human-readable label for this task, used by the SkyBlock XP source screens. */
    public String displayName() {
        return StringUtility.toNormalCase(getClass().getSimpleName().replace("LevelCause", ""));
    }
}
