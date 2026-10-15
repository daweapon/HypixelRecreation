package dev.daweapon.commons.protocol.objects.proxy.to;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

// Redis RPC protocol notifying the proxy that a server has finished processing a player and it may proceed with its transfer.
public class FinishedWithPlayerProtocol extends RedisProtocol<
        FinishedWithPlayerProtocol.Request,
        FinishedWithPlayerProtocol.Response> {
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

    public record Request(String uuid) {}
    public record Response() {}
}
