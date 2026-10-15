package dev.daweapon.commons.protocol.objects.proxy.from;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for asking a game server whether it is currently accepting new players.
public class IsServerJoinableProtocol extends RedisProtocol<
        IsServerJoinableProtocol.Request,
        IsServerJoinableProtocol.Response> {
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

    public record Request() {}
    public record Response(boolean joinable, boolean success, @Nullable String error) {}
}
