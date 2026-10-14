package net.swofty.service.auction;

import net.swofty.service.generic.MongoReadiness;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("net.swofty.service.auction.endpoints", RedisMessageHandler.class).toList();
    }
}
