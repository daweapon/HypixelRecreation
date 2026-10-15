package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;

// Persists a single boolean SkyBlock datapoint value.
public class DatapointSkyBlockBoolean extends SkyBlockDatapoint<Boolean> {
    private static final JacksonSerializer<Boolean> serializer = new JacksonSerializer<>(Boolean.class);

    public DatapointSkyBlockBoolean(String key, Boolean value) {
        super(key, value, serializer);
    }

    public DatapointSkyBlockBoolean(String key) {
        super(key, null, serializer);
    }
}