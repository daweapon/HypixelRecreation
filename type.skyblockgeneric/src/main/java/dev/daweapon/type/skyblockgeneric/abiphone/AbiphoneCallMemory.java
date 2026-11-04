package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class AbiphoneCallMemory {
    private AbiphoneCallMemory() {
    }

    private static DatapointStringList datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ABIPHONE_CALLS_MADE, DatapointStringList.class);
    }

    public static boolean has(SkyBlockPlayer player, String key) {
        return datapoint(player).getValue().contains(key);
    }

    public static void add(SkyBlockPlayer player, String key) {
        DatapointStringList datapoint = datapoint(player);
        if (datapoint.getValue().contains(key)) return;
        List<String> keys = new ArrayList<>(datapoint.getValue());
        keys.add(key);
        datapoint.setValue(keys);
    }

    public static void remove(SkyBlockPlayer player, String key) {
        removeIf(player, key::equals);
    }

    public static void removeIf(SkyBlockPlayer player, Predicate<String> filter) {
        DatapointStringList datapoint = datapoint(player);
        if (datapoint.getValue().stream().noneMatch(filter)) return;
        List<String> keys = new ArrayList<>(datapoint.getValue());
        keys.removeIf(filter);
        datapoint.setValue(keys);
    }
}
