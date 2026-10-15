package dev.daweapon.commons.protocol.objects.proxy.to;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

// Redis RPC protocol for backend-server-initiated player actions on the proxy: transfer, teleport, limbo, messaging, and status lookups.
public class PlayerHandlerProtocol extends RedisProtocol<
        PlayerHandlerProtocol.Request,
        PlayerHandlerProtocol.Response> {
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

    public enum Action {
        TRANSFER,
        RESOLVE_TRANSFER,
        TELEPORT,
        VERSION,
        IS_ONLINE,
        EVENT,
        MESSAGE,
        TRANSFER_WITH_UUID,
        GET_SERVER,
        LIMBO
    }

    public record Request(String uuid, Action action, Map<String, Object> data) {}
    public record Response(Map<String, Object> data, boolean success, @Nullable String error) {}
}
