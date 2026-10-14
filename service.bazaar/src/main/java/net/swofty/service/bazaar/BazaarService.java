package net.swofty.service.bazaar;

import net.swofty.service.generic.MongoReadiness;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("net.swofty.service.bazaar.endpoints", RedisMessageHandler.class).toList();
    }
}