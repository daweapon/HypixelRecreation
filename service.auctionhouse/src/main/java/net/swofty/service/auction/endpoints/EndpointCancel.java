package net.swofty.service.auction.endpoints;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.swofty.commons.protocol.objects.auctions.AuctionCancelProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.service.auction.AuctionActiveDatabase;
import net.swofty.service.auction.AuctionInactiveDatabase;
import net.swofty.service.auction.AuctionProfiles;
import org.bson.Document;

public class EndpointCancel implements RedisMessageHandler<AuctionCancelProtocol.Request, AuctionCancelProtocol.Response> {
    private final AuctionProfiles profiles;
    public EndpointCancel() { this(new AuctionProfiles()); }
    EndpointCancel(AuctionProfiles profiles) { this.profiles = profiles; }
    @Override public AuctionCancelProtocol protocol() { return new AuctionCancelProtocol(); }

    @Override public AuctionCancelProtocol.Response handle(AuctionCancelProtocol.Request request, RedisMessageContext context) {
        if (request.auction() == null || request.player() == null || request.profile() == null)
            return rejected("Invalid cancellation request.");
        Document stored = AuctionActiveDatabase.collection.find(Filters.eq("_id", request.auction().toString())).first();
        if (stored == null) {
            stored = AuctionInactiveDatabase.collection.find(Filters.eq("_id", request.auction().toString())).first();
            if (stored == null) return rejected("This auction could not be found.");
        }
        AuctionItem item = AuctionItem.fromDocument(stored);
        var sellerProfile = profiles.sellerProfile(item);
        if (!request.player().equals(item.getOriginator()) || !request.profile().equals(sellerProfile)
                || !profiles.owns(request.player(), request.profile())) return rejected("This is not your auction.");
        if (!item.getBids().isEmpty()) return rejected("Auctions with bids cannot be cancelled.");
        if (item.getEndTime() <= System.currentTimeMillis()) return new AuctionCancelProtocol.Response(true, null);
        long now = System.currentTimeMillis();
        var changed = AuctionActiveDatabase.collection.updateOne(Filters.and(
                Filters.eq("_id", request.auction().toString()), Filters.eq("originator", request.player().toString()),
                Filters.eq("seller_profile", request.profile().toString()), Filters.eq("end", stored.getLong("end")),
                Filters.size("bids", 0)), Updates.set("end", now));
        return changed.getModifiedCount() == 1 ? new AuctionCancelProtocol.Response(true, null)
                : rejected("This auction changed. Reopen it and try again.");
    }

    private static AuctionCancelProtocol.Response rejected(String error) {
        return new AuctionCancelProtocol.Response(false, error);
    }
}
