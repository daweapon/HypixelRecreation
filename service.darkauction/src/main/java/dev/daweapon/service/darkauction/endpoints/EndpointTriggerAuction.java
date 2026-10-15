package dev.daweapon.service.darkauction.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.darkauction.TriggerDarkAuctionProtocol;
import dev.daweapon.service.darkauction.DarkAuctionScheduler;
import dev.daweapon.service.darkauction.DarkAuctionService;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that starts a new Dark Auction (e.g. from a calendar trigger), ignoring the request if one is already active.
public class EndpointTriggerAuction implements RedisMessageHandler<
        TriggerDarkAuctionProtocol.TriggerMessage,
        TriggerDarkAuctionProtocol.TriggerResponse> {

    @Override
    public RedisProtocol<TriggerDarkAuctionProtocol.TriggerMessage, TriggerDarkAuctionProtocol.TriggerResponse> protocol() {
        return new TriggerDarkAuctionProtocol();
    }

    @Override
    public TriggerDarkAuctionProtocol.TriggerResponse handle(TriggerDarkAuctionProtocol.TriggerMessage msg, RedisMessageContext context) {

        Logger.info("Received trigger request - calendarTime: {}, forced: {}", msg.calendarTime(), msg.forced());

        // Check if auction is already active (deduplication)
        if (DarkAuctionService.hasActiveAuction()) {
            Logger.info("Auction already active, ignoring trigger");
            return new TriggerDarkAuctionProtocol.TriggerResponse(false, "Auction already in progress", null);
        }

        // Start the auction
        try {
            DarkAuctionScheduler.startNewAuction();
            Logger.info("Dark Auction started successfully via calendar trigger");
            return new TriggerDarkAuctionProtocol.TriggerResponse(true, "Auction started", null);
        } catch (Exception e) {
            Logger.error(e, "Failed to start Dark Auction");
            return new TriggerDarkAuctionProtocol.TriggerResponse(false, "Failed to start: " + e.getMessage(), null);
        }
    }
}
