package net.swofty.type.skyblockgeneric.hunting;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Attribute levels (wiki: SkyBlock Levels/Tasks): +1 SkyBlock XP for every level of every attribute, summed.
 */
public final class AttributeLevelAwards {
    public static final int XP_PER_LEVEL = 1;

    private AttributeLevelAwards() {
    }

    public static int skyBlockXp(DatapointHunting.HuntingData data) {
        int levels = 0;
        for (AttributeDefinition definition : AttributeRegistry.values()) levels += data.level(definition.id());
        return Math.min(levels * XP_PER_LEVEL, SkyBlockXPSource.ATTRIBUTE_LEVELS.getMaxXP());
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ATTRIBUTE_LEVELS, skyBlockXp(player.getHuntingData()));
    }
}
