package dev.daweapon.commons.protocol.objects.proxy.from;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

// Redis RPC protocol instructing a server to run a named ProxyUnderstandableEvent for a player with serialized event data.
public class RunEventProtocol extends RedisProtocol<
        RunEventProtocol.Request,
        RunEventProtocol.Response> {
    private static final Serializer<Request> SERIALIZER =
            new JacksonSerializer<>(Request.class);
    private static final Serializer<Response> RETURN_SERIALIZER =
            new JacksonSerializer<>(Response.class);

    @Override
    public Serializer<Request> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<Response> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record Request(String uuid, String event, String data) {}
    public record Response() {}
}
