package dev.daweapon.type.generic.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint wrapper for a plain Byte value.
public class DatapointByte extends Datapoint<Byte> {
    private static final JacksonSerializer<Byte> serializer = new JacksonSerializer<>(Byte.class);

    public DatapointByte(String key, Byte value) {
        super(key, value, serializer);
    }

    public DatapointByte(String key) {
        super(key, null, serializer);
    }
}
