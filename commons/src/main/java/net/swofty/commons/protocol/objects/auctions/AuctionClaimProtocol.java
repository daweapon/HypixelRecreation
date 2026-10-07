package net.swofty.commons.protocol.objects.auctions;

import net.swofty.commons.protocol.*;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class AuctionClaimProtocol extends RedisProtocol<AuctionClaimProtocol.Request, AuctionClaimProtocol.Response> {
    private static final Serializer<Request> SERIALIZER = new JacksonSerializer<>(Request.class);
    private static final Serializer<Response> RETURN_SERIALIZER = new JacksonSerializer<>(Response.class);
    @Override public Serializer<Request> getSerializer() { return SERIALIZER; }
    @Override public Serializer<Response> getReturnSerializer() { return RETURN_SERIALIZER; }
    public record Request(UUID auction, UUID player, UUID profile, int collectionTaxMultiplier) {
        public Request(UUID auction, UUID player, UUID profile) {
            this(auction, player, profile, 1);
        }
    }
    public record Response(boolean success, @Nullable String error, @Nullable Receipt receipt) {}
    public record Receipt(UUID id, UUID auction, UUID player, UUID profile, boolean seller,
                          long coins, @Nullable String item, long salePrice, boolean sold,
                          @Nullable String auctionItem) {
        public Receipt(UUID id, UUID auction, UUID player, UUID profile, boolean seller, long coins, @Nullable String item) {
            this(id, auction, player, profile, seller, coins, item, 0, false, null);
        }
    }
}
