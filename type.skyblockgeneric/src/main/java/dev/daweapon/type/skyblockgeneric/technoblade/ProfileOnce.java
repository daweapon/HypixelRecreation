package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public enum ProfileOnce {
    PIG_MASK("technoblade.pig_mask@PROFILE:1"),
    FLYING_PIG("technoblade.flying_pig@PROFILE:1");

    private final String key;

    ProfileOnce(String key) {
        this.key = key;
    }

    public boolean done(SkyBlockPlayer player) {
        return stored(datapoint(player)).get(key) instanceof Number number && number.longValue() > 0;
    }

    public void record(SkyBlockPlayer player) {
        DatapointMapStringLong datapoint = datapoint(player);
        Map<String, Long> values = new HashMap<>();
        stored(datapoint).forEach((entryKey, value) -> {
            if (value instanceof Number number) values.put(entryKey, number.longValue());
        });
        values.put(key, 1L);
        datapoint.setValue(values);
    }

    private static DatapointMapStringLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SHOP_PURCHASE_LIMITS, DatapointMapStringLong.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> stored(DatapointMapStringLong datapoint) {
        return (Map<String, ?>) (Map<?, ?>) datapoint.getValue();
    }
}
