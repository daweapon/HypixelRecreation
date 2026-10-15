package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint wrapper for a plain Double value.
public class DatapointDouble extends Datapoint<Double> {
    private static final JacksonSerializer<Double> serializer = new JacksonSerializer<>(Double.class);

    public DatapointDouble(String key, Double value) {
        super(key, value, serializer);
    }

    public DatapointDouble(String key) {
        super(key, null, serializer);
    }
}