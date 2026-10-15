package dev.daweapon.commons.protocol.objects;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

public class PingProtocol extends RedisProtocol<
        PingProtocol.EmptyMessage,
        PingProtocol.EmptyMessage> {
    private static final Serializer<EmptyMessage> SERIALIZER =
            new JacksonSerializer<>(EmptyMessage.class);
    private static final Serializer<EmptyMessage> RETURN_SERIALIZER =
            new JacksonSerializer<>(EmptyMessage.class);

    @Override
    public Serializer<EmptyMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<EmptyMessage> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record EmptyMessage() {}
}
