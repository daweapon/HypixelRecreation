package net.swofty.type.skyblockgeneric.levels.causes;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class CollectedMobTypesCause extends SkyBlockLevelCauseAbstr {
    public static final String ALL_TYPES = "ALL";
    public static final double ALL_TYPES_XP = 10;

    private String mobType;

    public CollectedMobTypesCause(String mobType) {
        this.mobType = mobType;
    }

    @Override
    public String displayName() {
        return isAllTypes() ? "All Mob Types" : StringUtility.toNormalCase(mobType);
    }

    @Override
    public double xpReward() {
        return isAllTypes() ? ALL_TYPES_XP : 1;
    }

    private boolean isAllTypes() {
        return ALL_TYPES.equals(mobType);
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }
}
