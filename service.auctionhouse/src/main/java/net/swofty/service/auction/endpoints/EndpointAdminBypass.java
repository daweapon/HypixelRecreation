package net.swofty.service.auction.endpoints;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.swofty.commons.protocol.objects.auctions.AuctionAdminBypassProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.skyblock.auctions.AuctionRules;
import net.swofty.service.auction.AuctionActiveDatabase;
import net.swofty.service.auction.AuctionService;
import org.bson.Document;

public class EndpointAdminBypass implements RedisMessageHandler<AuctionAdminBypassProtocol.Request, AuctionAdminBypassProtocol.Response> {
    @Override public AuctionAdminBypassProtocol protocol() { return new AuctionAdminBypassProtocol(); }

    @Override
    public AuctionAdminBypassProtocol.Response handle(AuctionAdminBypassProtocol.Request request, RedisMessageContext context) {
        if (request.auction() == null || request.timer() == null) return rejected("Invalid timer request.");
        String auctionId = request.auction().toString();
        Document stored = AuctionActiveDatabase.collection.find(Filters.eq("_id", auctionId)).first();
        if (stored == null) return rejected("This auction is not active or could not be found.");

        long now = System.currentTimeMillis();
        var result = switch (request.timer()) {
            case AUCTION_END -> AuctionActiveDatabase.collection.updateOne(
                    Filters.and(Filters.eq("_id", auctionId), Filters.gt("end", now)),
                    Updates.set("end", now));
            case BIN_GRACE -> {
                if (!Boolean.TRUE.equals(stored.getBoolean("bin"))) yield null;
                yield AuctionActiveDatabase.collection.updateOne(
                        Filters.and(Filters.eq("_id", auctionId), Filters.eq("bin", true)),
                        Updates.set("created_at", now - AuctionRules.BIN_PURCHASE_GRACE));
            }
        };

        if (result == null) return rejected("Only BIN auctions have a purchase grace timer.");
        if (result.getMatchedCount() != 1) return rejected("That timer already elapsed or the auction changed.");
        if (AuctionService.cacheService != null) AuctionService.cacheService.invalidateAll();
        return new AuctionAdminBypassProtocol.Response(true, null);
    }

    private static AuctionAdminBypassProtocol.Response rejected(String error) {
        return new AuctionAdminBypassProtocol.Response(false, error);
    }
}
