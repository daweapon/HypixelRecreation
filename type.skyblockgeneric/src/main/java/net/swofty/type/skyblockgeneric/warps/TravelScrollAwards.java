package net.swofty.type.skyblockgeneric.warps;

import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collection;
import java.util.Set;

/**
 * SkyBlock XP for unlocking fast travel scrolls (wiki: SkyBlock_Levels/Tasks, Fast Travels Unlocked): 15 per scroll,
 * 360 in total. The unobtainable Dragontail and Scarleton scrolls give nothing.
 */
public final class TravelScrollAwards {
    public static final int XP_PER_SCROLL = 15;
    private static final Set<TravelScrollType> UNOBTAINABLE =
            Set.of(TravelScrollType.CRIMSON_ISLE_DRAGONTAIL, TravelScrollType.CRIMSON_ISLE_SCARLETON);

    private TravelScrollAwards() {
    }

    public static int skyBlockXp(Collection<String> usedScrolls) {
        int count = 0;
        for (TravelScrollType type : TravelScrollType.values()) {
            if (!UNOBTAINABLE.contains(type) && usedScrolls.contains(type.getInternalName())) count++;
        }
        return count * XP_PER_SCROLL;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        var scrolls = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.USED_SCROLLS, DatapointStringList.class).getValue();
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.FAST_TRAVELS_UNLOCKED, skyBlockXp(scrolls));
    }
}
