package dev.daweapon.service.itemtracker;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice that tracks unique items and which players have owned them, backed by MongoDB.
public class ItemTrackerService implements SkyBlockService {
    static void main(String[] args) {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        // Connect the DB before the service starts handling Redis requests,
        // otherwise early messages hit a null collection and NPE.
        TrackedItemsDatabase.connect(ConfigProvider.settings().getMongodb());

        SkyBlockService.init(new ItemTrackerService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.ITEM_TRACKER;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.itemtracker.endpoints", RedisMessageHandler.class).toList();
    }
}
