package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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

    public static int maxSkyBlockXp() {
        int levels = 0;
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            if (definition.isAvailable()) levels += AttributeRarity.MAX_LEVEL;
        }
        return Math.min(levels * XP_PER_LEVEL, SkyBlockXPSource.ATTRIBUTE_LEVELS.getMaxXP());
    }

    /** Every available attribute syphoned to its maximum level. Shards stay unowned. */
    public static void maxOut(SkyBlockPlayer player) {
        DatapointHunting.HuntingData data = player.getHuntingData();
        for (AttributeDefinition definition : AttributeRegistry.values()) {
            if (!definition.isAvailable()) continue;
            String id = definition.id().toString();
            data.setSyphoned(id, Math.max(data.syphoned(id), definition.rarity().cumulativeForLevel(AttributeRarity.MAX_LEVEL)));
        }
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ATTRIBUTE_LEVELS, skyBlockXp(player.getHuntingData()));
    }
}
