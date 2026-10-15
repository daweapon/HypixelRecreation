package dev.daweapon.commons.protocol.objects.auctions;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionsFilter;
import dev.daweapon.commons.skyblock.auctions.AuctionsSorting;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class AuctionFetchItemsProtocol extends RedisProtocol<
        AuctionFetchItemsProtocol.AuctionFetchItemsMessage,
        AuctionFetchItemsProtocol.AuctionFetchItemsResponse> {
    private static final Serializer<AuctionFetchItemsMessage> SERIALIZER =
            new JacksonSerializer<>(AuctionFetchItemsMessage.class);
    private static final Serializer<AuctionFetchItemsResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(AuctionFetchItemsResponse.class);

    @Override
    public Serializer<AuctionFetchItemsMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<AuctionFetchItemsResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record AuctionFetchItemsMessage(
            AuctionsSorting sorting,
            AuctionsFilter filter,
            AuctionCategories category
    ) {}

    public record AuctionFetchItemsResponse(
            List<AuctionItem> items,
            boolean success,
            @Nullable String error
    ) {}
}
