package dev.daweapon.service.bazaar;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice for the bazaar: connects MongoDB, restores persisted orders into the market, and serves bazaar endpoints.
public class BazaarService implements SkyBlockService {

    static void main(String[] args) {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        // Connect to MongoDB for orders BEFORE the service starts handling Redis
        // requests, otherwise early messages hit a null OrderDatabase and NPE.
        OrderDatabase.connect(ConfigProvider.settings().getMongodb());

        // Initialize the bazaar market (loads existing orders)
        BazaarMarket.get();
        // load persisted orders
        OrderRepository.loadAll();

        SkyBlockService.init(new BazaarService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.BAZAAR;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.bazaar.endpoints", RedisMessageHandler.class).toList();
    }
}