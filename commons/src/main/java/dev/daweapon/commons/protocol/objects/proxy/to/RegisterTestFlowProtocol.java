package dev.daweapon.commons.protocol.objects.proxy.to;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.Map;

// Redis RPC protocol for registering a new isolated test flow and its expected server configuration with the proxy.
public class RegisterTestFlowProtocol extends RedisProtocol<
        RegisterTestFlowProtocol.Request,
        RegisterTestFlowProtocol.Response> {
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

    public record Request(String testFlowName, String handler, List<String> players,
                          List<Map<String, Object>> serverConfigs) {}
    public record Response(boolean success, String message, String error) {}
}
