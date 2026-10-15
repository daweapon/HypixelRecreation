package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

public class DatapointString extends Datapoint<String> {
    private static final JacksonSerializer<String> serializer = new JacksonSerializer<>(String.class);

    public DatapointString(String key, String value) {
        super(key, value, serializer);
    }

    public DatapointString(String key) {
        super(key, null, serializer);
    }
}