package dev.daweapon.type.skyblockgeneric.theend;

import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ZealotPity {
    private ZealotPity() {
    }

    public static int meter(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static int increment(SkyBlockPlayer player) {
        DatapointInteger datapoint = datapoint(player);
        datapoint.setValue(datapoint.getValue() + 1);
        return datapoint.getValue();
    }

    public static void reset(SkyBlockPlayer player) {
        datapoint(player).setValue(0);
    }

    private static DatapointInteger datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ZEALOT_PITY, DatapointInteger.class);
    }
}
