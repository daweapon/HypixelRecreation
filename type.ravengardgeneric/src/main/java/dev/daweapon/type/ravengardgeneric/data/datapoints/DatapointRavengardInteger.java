package dev.daweapon.type.ravengardgeneric.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.ravengardgeneric.data.RavengardDatapoint;

public class DatapointRavengardInteger extends RavengardDatapoint<Integer> {
    private static final JacksonSerializer<Integer> serializer = new JacksonSerializer<>(Integer.class);

    public DatapointRavengardInteger(String key, Integer value) {
        super(key, value, serializer);
    }

    public DatapointRavengardInteger(String key) {
        super(key, null, serializer);
    }
}
