package dev.daweapon.service.auction.endpoints;

import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionsFilter;
import dev.daweapon.commons.skyblock.auctions.AuctionsSorting;
import dev.daweapon.commons.protocol.objects.auctions.AuctionFetchItemsProtocol;
import dev.daweapon.service.auction.AuctionService;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that fetches, filters, and sorts auction listings for a category.
public class EndpointFetchItems implements RedisMessageHandler<
        AuctionFetchItemsProtocol.AuctionFetchItemsMessage,
        AuctionFetchItemsProtocol.AuctionFetchItemsResponse> {

    @Override
    public AuctionFetchItemsProtocol protocol() {
        return new AuctionFetchItemsProtocol();
    }

    @Override
    public AuctionFetchItemsProtocol.AuctionFetchItemsResponse handle(AuctionFetchItemsProtocol.AuctionFetchItemsMessage messageObject, RedisMessageContext context) {
        AuctionsSorting sorting = messageObject.sorting();
        AuctionsFilter filter = messageObject.filter();
        AuctionCategories category = messageObject.category();

        List<Document> results = new ArrayList<>();
        for (Document document : AuctionService.cacheService.getAuctions(category.toString(), filter)) {
            try {
                AuctionItem.fromDocument(document);
                results.add(document);
            } catch (RuntimeException invalid) {
                System.err.println("Skipping unreadable auction " + document.get("_id"));
            }
        }

        if (results.isEmpty()) {
            return new AuctionFetchItemsProtocol.AuctionFetchItemsResponse(new ArrayList<>(), true, null);
        }

        // Sort according to sorting
        switch (sorting) {
            case HIGHEST_BID:
                results.sort((o1, o2) -> Long.compare(currentPrice(o2), currentPrice(o1)));
                break;
            case LOWEST_BID:
                results.sort((o1, o2) -> Long.compare(currentPrice(o1), currentPrice(o2)));
                break;
            case ENDING_SOON:
                results.sort((o1, o2) -> {
                    long time1 = o1.getLong("end");
                    long time2 = o2.getLong("end");
                    return Long.compare(time1, time2);
                });
                break;
            case MOST_BIDS:
                results.sort((o1, o2) -> {
                    int bids1 = o1.getList("bids", String.class).size();
                    int bids2 = o2.getList("bids", String.class).size();
                    return Integer.compare(bids2, bids1);
                });
                break;
        }

        List<AuctionItem> items = new ArrayList<>(results.size());
        for (Document document : results) {
            try {
                items.add(AuctionItem.fromDocument(document));
            } catch (Exception e) {
                System.err.println("Skipping unreadable auction _id=" + document.get("_id") + " in category " + category);
                e.printStackTrace();
            }
        }
        return new AuctionFetchItemsProtocol.AuctionFetchItemsResponse(items, true, null);
    }

    private static long currentPrice(Document document) {
        List<String> bids = document.getList("bids", String.class);
        if (bids != null && !bids.isEmpty()) {
            long highest = Long.MIN_VALUE;
            for (String bid : bids) {
                highest = Math.max(highest, AuctionItem.Bid.fromString(bid).value());
            }
            return highest;
        }
        return ((Number) document.get("starting-price")).longValue();
    }
}
