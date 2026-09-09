package net.swofty.service.auction.endpoints;

import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.protocol.objects.auctions.AuctionFetchItemProtocol;
import net.swofty.service.auction.AuctionActiveDatabase;
import net.swofty.service.auction.AuctionInactiveDatabase;
import net.swofty.commons.redis.RedisMessageHandler;
import org.bson.Document;

import java.util.UUID;
import net.swofty.commons.redis.RedisMessageContext;

// Redis endpoint that fetches a single auction listing by id, checking active listings first then inactive ones.
public class EndpointFetchItem implements RedisMessageHandler<
        AuctionFetchItemProtocol.AuctionFetchItemMessage,
        AuctionFetchItemProtocol.AuctionFetchItemResponse> {

    @Override
    public AuctionFetchItemProtocol protocol() {
        return new AuctionFetchItemProtocol();
    }

    @Override
    public AuctionFetchItemProtocol.AuctionFetchItemResponse handle(AuctionFetchItemProtocol.AuctionFetchItemMessage messageObject, RedisMessageContext context) {
        UUID uuidToFetch = messageObject.uuid();

        Document item = AuctionActiveDatabase.collection.find(new Document("_id", uuidToFetch.toString())).first();
        if (item == null) item = AuctionInactiveDatabase.collection.find(new Document("_id", uuidToFetch.toString())).first();
        if (item == null) return new AuctionFetchItemProtocol.AuctionFetchItemResponse(null, false, "This auction could not be found.");
        return new AuctionFetchItemProtocol.AuctionFetchItemResponse(AuctionItem.fromDocument(item), true, null);
    }
}
