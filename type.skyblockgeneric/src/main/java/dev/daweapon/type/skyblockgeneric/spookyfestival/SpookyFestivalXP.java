package dev.daweapon.type.skyblockgeneric.spookyfestival;

import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class SpookyFestivalXP {
    public static final int[] BRACKET_XP = {10, 20, 30, 40, 50, 75};
    public static final int POINT_BRACKETS = BRACKET_XP.length;

    private SpookyFestivalXP() {
    }

    public static int bracketsReached(int points) {
        int reached = 0;
        for (int i = 0; i < POINT_BRACKETS; i++) {
            if (points >= SpookyFestivalTiers.TIERS[i].threshold()) reached = i + 1;
        }
        return reached;
    }

    public static int total(int bracketsReached) {
        int xp = 0;
        for (int i = 0; i < Math.min(bracketsReached, POINT_BRACKETS); i++) xp += BRACKET_XP[i];
        return xp;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        DatapointInteger best = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_BEST_BRACKET, DatapointInteger.class);
        int reached = Math.max(best.getValue(), bracketsReached(SpookyFestivalCandyTally.of(player).points()));
        if (reached != best.getValue()) best.setValue(reached);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.SPOOKY_FESTIVAL, total(reached));
    }
}
