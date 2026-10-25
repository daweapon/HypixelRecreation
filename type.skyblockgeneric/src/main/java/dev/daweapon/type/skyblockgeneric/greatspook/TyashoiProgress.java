package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public final class TyashoiProgress {
    private static final String CLAIMED = "claimed.";
    private static final String LOCKOUT = "lockout.";
    private static final String INTRO = "intro";

    private TyashoiProgress() {
    }

    private static DatapointMapStringLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.GREAT_SPOOK_EXPERIMENTS, DatapointMapStringLong.class);
    }

    private static void put(SkyBlockPlayer player, String key, long value) {
        DatapointMapStringLong datapoint = datapoint(player);
        Map<String, Long> updated = new HashMap<>(datapoint.getValue());
        updated.put(key, value);
        datapoint.setValue(updated);
    }

    public static boolean claimed(SkyBlockPlayer player, String id) {
        return datapoint(player).getValue().containsKey(CLAIMED + id);
    }

    public static void claim(SkyBlockPlayer player, String id) {
        put(player, CLAIMED + id, System.currentTimeMillis());
    }

    public static long lockedUntil(SkyBlockPlayer player, String id) {
        return datapoint(player).getValue().getOrDefault(LOCKOUT + id, 0L);
    }

    public static void lock(SkyBlockPlayer player, String id, long untilMillis) {
        put(player, LOCKOUT + id, untilMillis);
    }

    public static boolean introduced(SkyBlockPlayer player) {
        return datapoint(player).getValue().containsKey(INTRO);
    }

    public static void introduce(SkyBlockPlayer player) {
        put(player, INTRO, System.currentTimeMillis());
    }

    public static void reset(SkyBlockPlayer player) {
        datapoint(player).setValue(new HashMap<>());
    }
}
