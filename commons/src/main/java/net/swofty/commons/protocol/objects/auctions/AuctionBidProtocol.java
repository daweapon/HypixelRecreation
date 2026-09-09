package net.swofty.commons.protocol.objects.auctions;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AuctionBidProtocol extends RedisProtocol<AuctionBidProtocol.Request, AuctionBidProtocol.Response> {
    private static final Serializer<Request> SERIALIZER = new JacksonSerializer<>(Request.class);
    private static final Serializer<Response> RESPONSE_SERIALIZER = new JacksonSerializer<>(Response.class);

    @Override public Serializer<Request> getSerializer() { return SERIALIZER; }
    @Override public Serializer<Response> getReturnSerializer() { return RESPONSE_SERIALIZER; }

    public record Request(UUID operation, UUID player, UUID profile, AuctionAddItemProtocol.AuctionAddItemMessage bid) {}
    public record Response(UUID operation, UUID auction, UUID profile, boolean success, @Nullable String error) {}
}
