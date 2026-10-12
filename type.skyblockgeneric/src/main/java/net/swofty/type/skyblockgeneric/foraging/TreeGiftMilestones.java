package net.swofty.type.skyblockgeneric.foraging;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTreeGifts;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

/**
 * Tree Gift milestones (wiki: Swoop): each of 7 tiers per tree type (10, 25, 100, 250, 500, 1,000 and 2,500 gifts)
 * grants +4 SkyBlock XP. The wiki lists Fig and Mangrove only; Helix uses the same tiers to reach the 84 XP cap.
 */
public final class TreeGiftMilestones {
    public static final List<String> TREE_TYPES = List.of("FIG", "MANGROVE", "HELIX");
    public static final int[] TIER_GIFTS = {10, 25, 100, 250, 500, 1000, 2500};
    public static final int XP_PER_MILESTONE = 4;

    private TreeGiftMilestones() {
    }

    public static int milestones(int gifts) {
        int reached = 0;
        for (int required : TIER_GIFTS) if (gifts >= required) reached++;
        return reached;
    }

    public static int skyBlockXp(Map<String, Integer> counts) {
        int total = 0;
        for (String type : TREE_TYPES) total += milestones(counts.getOrDefault(type, 0)) * XP_PER_MILESTONE;
        return total;
    }

    public static void recordGift(SkyBlockPlayer player, String treeType) {
        datapoint(player).increment(treeType);
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.TREE_GIFT_MILESTONES,
                skyBlockXp(datapoint(player).getValue()));
    }

    private static DatapointTreeGifts datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TREE_GIFTS, DatapointTreeGifts.class);
    }
}
