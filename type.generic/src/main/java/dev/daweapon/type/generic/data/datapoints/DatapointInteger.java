package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint wrapper for a plain Integer value.
public class DatapointInteger extends Datapoint<Integer> {
    private static final JacksonSerializer<Integer> serializer = new JacksonSerializer<>(Integer.class);

    public DatapointInteger(String key, Integer value) {
        super(key, value, serializer);
    }

    public DatapointInteger(String key) {
        super(key, null, serializer);
    }
}
