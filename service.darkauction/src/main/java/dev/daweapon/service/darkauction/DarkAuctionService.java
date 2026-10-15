package dev.daweapon.service.darkauction;

import dev.daweapon.commons.ServiceType;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice hosting the Dark Auction: starts the scheduler and holds the single currently-running auction, if any.
public class DarkAuctionService implements SkyBlockService {
    private static DarkAuctionState currentAuction = null;

    public static void main(String[] args) {
        DarkAuctionScheduler.start();

        SkyBlockService.init(new DarkAuctionService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.DARK_AUCTION;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.darkauction.endpoints", RedisMessageHandler.class).toList();
    }

    public static DarkAuctionState getCurrentAuction() {
        return currentAuction;
    }

    public static void setCurrentAuction(DarkAuctionState auction) {
        currentAuction = auction;
    }

    public static boolean hasActiveAuction() {
        return currentAuction != null;
    }
}
