package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointFrozenCorpses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CorpseProgress {
    private CorpseProgress() {
    }

    public static DatapointFrozenCorpses datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FROZEN_CORPSES, DatapointFrozenCorpses.class);
    }

    public static DatapointFrozenCorpses.FrozenCorpseData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, DatapointFrozenCorpses.FrozenCorpseData data) {
        datapoint(player).setValue(data);
    }

    public static int lootedCount(SkyBlockPlayer player, CorpseType type) {
        return data(player).lootedCount(type.name());
    }
}
