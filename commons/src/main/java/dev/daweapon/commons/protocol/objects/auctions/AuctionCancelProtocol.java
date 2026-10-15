package dev.daweapon.commons.protocol.objects.auctions;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AuctionCancelProtocol extends RedisProtocol<AuctionCancelProtocol.Request, AuctionCancelProtocol.Response> {
    private static final Serializer<Request> SERIALIZER = new JacksonSerializer<>(Request.class);
    private static final Serializer<Response> RETURN_SERIALIZER = new JacksonSerializer<>(Response.class);
    @Override public Serializer<Request> getSerializer() { return SERIALIZER; }
    @Override public Serializer<Response> getReturnSerializer() { return RETURN_SERIALIZER; }
    public record Request(UUID auction, UUID player, UUID profile) {}
    public record Response(boolean success, @Nullable String error) {}
}
