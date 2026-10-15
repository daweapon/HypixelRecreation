package dev.daweapon.service.auction;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice for the auction house: connects the active/inactive auction databases and serves auction endpoints.
public class AuctionService implements SkyBlockService {
    public static AuctionsCacheService cacheService;

    static void main(String[] args) {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        new AuctionActiveDatabase("_placeholder").connect(ConfigProvider.settings().getMongodb());
        new AuctionInactiveDatabase("_placeholder").connect(ConfigProvider.settings().getMongodb());

        cacheService = new AuctionsCacheService();
        new AuctionArchiveService().start();

        SkyBlockService.init(new AuctionService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.AUCTION_HOUSE;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.auction.endpoints", RedisMessageHandler.class).toList();
    }
}
