package net.swofty.service.friend;

import net.swofty.service.generic.MongoReadiness;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice that owns friend lists, friend requests, and player presence, backed by MongoDB and Redis.
public class FriendService implements SkyBlockService {

    static void main() {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        String mongoUri = ConfigProvider.settings().getMongodb();
        new FriendDatabase(null).connect(mongoUri);

        FriendCache.startExpirationChecker();

        SkyBlockService.init(new FriendService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.FRIEND;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("net.swofty.service.friend.endpoints", RedisMessageHandler.class).toList();
    }
}
