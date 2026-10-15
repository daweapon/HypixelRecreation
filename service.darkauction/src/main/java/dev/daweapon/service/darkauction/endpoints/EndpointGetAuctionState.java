package dev.daweapon.service.darkauction.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.darkauction.GetAuctionStateProtocol;
import dev.daweapon.service.darkauction.DarkAuctionService;
import dev.daweapon.service.darkauction.DarkAuctionState;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that returns a snapshot of the current Dark Auction state, if one is active.
public class EndpointGetAuctionState implements RedisMessageHandler<
        GetAuctionStateProtocol.GetAuctionStateMessage,
        GetAuctionStateProtocol.GetAuctionStateResponse> {

    @Override
    public RedisProtocol<GetAuctionStateProtocol.GetAuctionStateMessage, GetAuctionStateProtocol.GetAuctionStateResponse> protocol() {
        return new GetAuctionStateProtocol();
    }

    @Override
    public GetAuctionStateProtocol.GetAuctionStateResponse handle(GetAuctionStateProtocol.GetAuctionStateMessage msg, RedisMessageContext context) {

        DarkAuctionState auction = DarkAuctionService.getCurrentAuction();

        if (auction == null) {
            return new GetAuctionStateProtocol.GetAuctionStateResponse(
                    false, null, null, 0, null, 0, null, null, List.of(), true, null
            );
        }

        return new GetAuctionStateProtocol.GetAuctionStateResponse(
                true,
                auction.getAuctionId(),
                auction.getPhase(),
                auction.getCurrentRound(),
                auction.getCurrentItem() != null ? auction.getCurrentItem().name() : null,
                auction.getCurrentBid(),
                auction.getHighestBidderId(),
                auction.getHighestBidderName(),
                auction.getRoundItemNames(),
                true,
                null
        );
    }
}
