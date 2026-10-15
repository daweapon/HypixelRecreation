package dev.daweapon.type.skyblockgeneric.achievement;

import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class AchievementFlags {
    private AchievementFlags() {
    }

    public static boolean has(SkyBlockPlayer player, String flag) {
        return datapoint(player).getValue().contains(flag);
    }

    public static boolean mark(SkyBlockPlayer player, String flag) {
        DatapointStringList datapoint = datapoint(player);
        if (datapoint.getValue().contains(flag)) return false;
        List<String> flags = new ArrayList<>(datapoint.getValue());
        flags.add(flag);
        datapoint.setValue(flags);
        return true;
    }

    private static DatapointStringList datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ACHIEVEMENT_FLAGS, DatapointStringList.class);
    }
}
