package net.swofty.commons.protocol.objects.proxy.from;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
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
