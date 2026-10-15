package dev.daweapon.commons.protocol.objects.proxy.to;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for a server to check with the proxy whether it (the requester) is currently registered/online.
public class ProxyIsOnlineProtocol extends RedisProtocol<
        ProxyIsOnlineProtocol.Request,
        ProxyIsOnlineProtocol.Response> {
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
    public record Response(boolean online, boolean success, @Nullable String error) {}
}
