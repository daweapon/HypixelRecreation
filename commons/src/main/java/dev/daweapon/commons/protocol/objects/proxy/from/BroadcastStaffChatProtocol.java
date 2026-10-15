package dev.daweapon.commons.protocol.objects.proxy.from;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

// Redis RPC protocol for pushing a staff chat message (or join/leave notice) from the proxy out to a backend server.
public class BroadcastStaffChatProtocol extends RedisProtocol<
        BroadcastStaffChatProtocol.Request,
        BroadcastStaffChatProtocol.Response> {
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

    public record Request(String type, String formattedMessage, String uuid) {}
    public record Response() {}
}
