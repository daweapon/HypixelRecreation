package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint wrapper for a plain Boolean value.
public class DatapointBoolean extends Datapoint<Boolean> {
    private static final JacksonSerializer<Boolean> serializer = new JacksonSerializer<>(Boolean.class);

    public DatapointBoolean(String key, Boolean value) {
        super(key, value, serializer);
    }

    public DatapointBoolean(String key) {
        super(key, null, serializer);
    }
}