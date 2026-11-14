package dev.daweapon.type.ravengardgeneric.data.datapoints;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.ravengardgeneric.data.RavengardDatapoint;

public class DatapointRavengardString extends RavengardDatapoint<String> {
    private static final JacksonSerializer<String> serializer = new JacksonSerializer<>(String.class);

    public DatapointRavengardString(String key, String value) {
        super(key, value, serializer);
    }

    public DatapointRavengardString(String key) {
        super(key, null, serializer);
    }
}
