package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint wrapper for a plain Float value.
public class DatapointFloat extends Datapoint<Float> {
    private static final JacksonSerializer<Float> serializer = new JacksonSerializer<>(Float.class);

    public DatapointFloat(String key, Float value) {
        super(key, value, serializer);
    }

    public DatapointFloat(String key) {
        super(key, null, serializer);
    }
}
