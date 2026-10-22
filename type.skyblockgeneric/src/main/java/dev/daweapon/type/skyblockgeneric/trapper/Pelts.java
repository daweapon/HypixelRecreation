package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class Pelts {
    private static final String BALANCE = "pelts";
    private static final String LIFETIME = "peltsEarned";

    private Pelts() {
    }

    private static DatapointHunting.HuntingData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class).getValue();
    }

    private static void save(SkyBlockPlayer player) {
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class);
        datapoint.setValue(datapoint.getValue());
    }

    public static long balance(SkyBlockPlayer player) {
        return data(player).counter(BALANCE);
    }

    public static long earned(SkyBlockPlayer player) {
        return data(player).counter(LIFETIME);
    }

    public static void add(SkyBlockPlayer player, long amount) {
        if (amount <= 0) return;
        var data = data(player);
        data.setCounter(BALANCE, data.counter(BALANCE) + amount);
        data.setCounter(LIFETIME, data.counter(LIFETIME) + amount);
        save(player);
    }

    public static boolean spend(SkyBlockPlayer player, long amount) {
        var data = data(player);
        if (amount < 0 || data.counter(BALANCE) < amount) return false;
        data.setCounter(BALANCE, data.counter(BALANCE) - amount);
        save(player);
        return true;
    }
}
