package dev.daweapon.commons.protocol.objects.proxy.from;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

// Redis RPC protocol telling a destination server what server type a transferring player is arriving from.
public class GivePlayersOriginTypeProtocol extends RedisProtocol<
        GivePlayersOriginTypeProtocol.Request,
        GivePlayersOriginTypeProtocol.Response> {
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

    public record Request(String uuid, String originType) {}
    public record Response() {}
}
