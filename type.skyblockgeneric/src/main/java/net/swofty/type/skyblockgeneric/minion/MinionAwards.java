package net.swofty.type.skyblockgeneric.minion;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

/**
 * SkyBlock XP for crafting each unique minion tier for the first time (wiki: SkyBlock_Levels/Tasks, Craft Minions):
 * tiers I-VI give 1, then 2, 3, 4, 6, 12 and 24 for tiers VII-XII.
 */
public final class MinionAwards {
    private static final int[] TIER_XP = {1, 1, 1, 1, 1, 1, 2, 3, 4, 6, 12, 24};

    private MinionAwards() {
    }

    public static int xpForTier(int tier) {
        return tier >= 1 && tier <= TIER_XP.length ? TIER_XP[tier - 1] : 0;
    }

    public static int skyBlockXp(DatapointMinionData.ProfileMinionData data) {
        int total = 0;
        for (Map.Entry<String, List<Integer>> minion : data.craftedMinions()) {
            for (int tier : minion.getValue().stream().distinct().toList()) total += xpForTier(tier);
        }
        return total;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        DatapointMinionData datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.MINION_DATA, DatapointMinionData.class);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.CRAFT_MINIONS, skyBlockXp(datapoint.getValue()));
    }
}
