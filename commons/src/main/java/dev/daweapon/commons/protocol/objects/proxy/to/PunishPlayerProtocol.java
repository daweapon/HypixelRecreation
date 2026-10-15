package dev.daweapon.commons.protocol.objects.proxy.to;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

// Redis RPC protocol instructing the proxy to apply a punishment (ban/mute/warning) to an online player.
public class PunishPlayerProtocol extends RedisProtocol<
        PunishPlayerProtocol.Request,
        PunishPlayerProtocol.Response> {
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

    public record Request(String target, String type, String id, long expiresAt,
                          String reasonBan, String reasonMute, String tags) {}
    public record Response() {}
}
