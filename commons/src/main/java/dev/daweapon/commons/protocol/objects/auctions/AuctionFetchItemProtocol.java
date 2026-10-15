package dev.daweapon.commons.protocol.objects.auctions;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class AuctionFetchItemProtocol extends RedisProtocol<
        AuctionFetchItemProtocol.AuctionFetchItemMessage,
        AuctionFetchItemProtocol.AuctionFetchItemResponse> {
    private static final Serializer<AuctionFetchItemMessage> SERIALIZER =
            new JacksonSerializer<>(AuctionFetchItemMessage.class);
    private static final Serializer<AuctionFetchItemResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(AuctionFetchItemResponse.class);

    @Override
    public Serializer<AuctionFetchItemMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<AuctionFetchItemResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record AuctionFetchItemMessage(UUID uuid) {}

    public record AuctionFetchItemResponse(@Nullable AuctionItem item, boolean success, @Nullable String error) {}
}
