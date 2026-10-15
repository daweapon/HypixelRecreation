package dev.daweapon.service.store;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.generic.SkyBlockService;

import java.util.List;

// Backend microservice for the in-game store; on startup connects MongoDB and starts the purchase fulfillment worker.
public class StoreService implements SkyBlockService {
    private final PurchaseFulfillmentWorker worker;

    public StoreService(PurchaseFulfillmentWorker worker) {
        this.worker = worker;
    }

    static void main() {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        String mongoUri = ConfigProvider.settings().getMongodb();
        StoreDatabase database = (StoreDatabase) new StoreDatabase(null).connect(mongoUri);

        SkyBlockService.init(new StoreService(new PurchaseFulfillmentWorker(database)));
    }

    @Override
    public ServiceType getType() {
        return ServiceType.STORE;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return List.of();
    }

    @Override
    public void onReady() {
        worker.start();
    }
}
