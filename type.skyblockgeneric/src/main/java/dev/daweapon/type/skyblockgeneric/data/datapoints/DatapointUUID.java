package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;

import java.util.UUID;

// Persists a single UUID SkyBlock datapoint value.
public class DatapointUUID extends SkyBlockDatapoint<UUID> {
    private static final JacksonSerializer<UUID> serializer = new JacksonSerializer<>(UUID.class);

    public DatapointUUID(String key, UUID value) {
        super(key, value, serializer);
    }

    public DatapointUUID(String key) {
        super(key, null, serializer);
    }
}