package net.swofty.commons.protocol.objects.proxy.from;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;

// Redis RPC protocol notifying a server that a player has fully switched away from it.
public class PlayerSwitchedProtocol extends RedisProtocol<
        PlayerSwitchedProtocol.Request,
        PlayerSwitchedProtocol.Response> {
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
