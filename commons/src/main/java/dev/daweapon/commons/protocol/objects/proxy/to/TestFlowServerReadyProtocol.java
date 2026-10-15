package dev.daweapon.commons.protocol.objects.proxy.to;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

// Redis RPC protocol for a server to signal the proxy that it is ready for its assigned test flow.
public class TestFlowServerReadyProtocol extends RedisProtocol<
        TestFlowServerReadyProtocol.Request,
        TestFlowServerReadyProtocol.Response> {
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

    public record Request(String testFlowName, String serverType, int serverIndex) {}
    public record Response(boolean success, String message, String error) {}
}
