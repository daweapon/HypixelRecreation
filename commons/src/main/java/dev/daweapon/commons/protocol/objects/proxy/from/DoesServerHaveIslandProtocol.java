package dev.daweapon.commons.protocol.objects.proxy.from;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for asking a SkyBlock island server whether it is currently hosting a given island.
public class DoesServerHaveIslandProtocol extends RedisProtocol<
        DoesServerHaveIslandProtocol.Request,
        DoesServerHaveIslandProtocol.Response> {
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

    public record Request(String islandUuid) {}
    public record Response(boolean serverHasIt, boolean success, @Nullable String error) {}
}
