package dev.daweapon.commons.protocol.objects.auctions;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class AuctionAddItemProtocol extends RedisProtocol
        <AuctionAddItemProtocol.AuctionAddItemMessage,
        AuctionAddItemProtocol.AuctionAddItemResponse> {
    private static final Serializer<AuctionAddItemMessage> SERIALIZER =
            new JacksonSerializer<>(AuctionAddItemMessage.class);
    private static final Serializer<AuctionAddItemResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(AuctionAddItemResponse.class);

    @Override

    public Serializer<AuctionAddItemMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<AuctionAddItemResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record AuctionAddItemMessage(AuctionItem item, AuctionCategories category, @Nullable ExpectedState expected) {
        public AuctionAddItemMessage(AuctionItem item, AuctionCategories category) {
            this(item, category, null);
        }
    }

    public record ExpectedState(java.util.List<AuctionItem.Bid> bids, long endTime) {
        public static ExpectedState of(AuctionItem item) {
            return new ExpectedState(java.util.List.copyOf(item.getBids()), item.getEndTime());
        }
    }

    public record AuctionAddItemResponse(UUID uuid, boolean success, @Nullable String error) {}
}
