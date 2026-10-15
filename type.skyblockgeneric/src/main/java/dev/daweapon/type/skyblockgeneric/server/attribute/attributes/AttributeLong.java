package dev.daweapon.type.skyblockgeneric.server.attribute.attributes;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.skyblockgeneric.server.attribute.ServerAttribute;

public class AttributeLong extends ServerAttribute<Long> {
    private static final JacksonSerializer<Long> serializer = new JacksonSerializer<>(Long.class);

    public AttributeLong(String key, Long value) {
        super(key, value, serializer);
    }

    public AttributeLong(String key) {
        super(key, null, serializer);
    }
}