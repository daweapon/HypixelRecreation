package net.swofty.commons.protocol.objects.proxy.to;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for querying player counts from the proxy, by network total, server type, or specific server UUID.
public class PlayerCountProtocol extends RedisProtocol<
        PlayerCountProtocol.Request,
        PlayerCountProtocol.Response> {
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

    public enum LookupType { ALL, TYPE, UUID }

    public record Request(LookupType lookupType, String lookupValue) {}
    public record Response(int playerCount, boolean success, @Nullable String error) {}
}
