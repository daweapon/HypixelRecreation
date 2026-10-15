package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Essence is a per-profile currency, not an item. Forest Essence predates this class and keeps
 * its own datapoint so existing profiles don't need migrating.
 */
public final class Essences {
    private Essences() {
    }

    public static long get(SkyBlockPlayer player, EssenceType type) {
        if (type == EssenceType.FOREST) return forest(player).getValue();
        Object value = stored(player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.ESSENCE, DatapointMapStringLong.class)).get(type.name());
        return value instanceof Number number ? number.longValue() : 0;
    }

    public static void set(SkyBlockPlayer player, EssenceType type, long amount) {
        long clamped = Math.clamp(amount, 0, EssenceType.CAP);
        if (type == EssenceType.FOREST) {
            forest(player).setValue(clamped);
            return;
        }
        DatapointMapStringLong datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.ESSENCE, DatapointMapStringLong.class);
        Map<String, Long> values = new HashMap<>();
        stored(datapoint).forEach((key, value) -> {
            if (value instanceof Number number) values.put(key, number.longValue());
        });
        values.put(type.name(), clamped);
        datapoint.setValue(values);
    }

    public static void add(SkyBlockPlayer player, EssenceType type, long amount) {
        set(player, type, get(player, type) + amount);
    }

    public static boolean take(SkyBlockPlayer player, EssenceType type, long amount) {
        long current = get(player, type);
        if (current < amount) return false;
        set(player, type, current - amount);
        return true;
    }

    /** Saved profiles can hold Integers in this map, so its values are read untyped. */
    @SuppressWarnings("unchecked")
    private static Map<String, ?> stored(DatapointMapStringLong datapoint) {
        return (Map<String, ?>) (Map<?, ?>) datapoint.getValue();
    }

    private static DatapointLong forest(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FOREST_ESSENCE, DatapointLong.class);
    }
}
