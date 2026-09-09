package net.swofty.type.generic.data.datapoints;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.type.generic.data.Datapoint;

// Datapoint wrapper for a plain Long value.
public class DatapointLong extends Datapoint<Long> {
    private static final JacksonSerializer<Long> serializer = new JacksonSerializer<>(Long.class);

    public DatapointLong(String key, Long value) {
        super(key, value, serializer);
    }

    public DatapointLong(String key) {
        super(key, null, serializer);
    }
}
