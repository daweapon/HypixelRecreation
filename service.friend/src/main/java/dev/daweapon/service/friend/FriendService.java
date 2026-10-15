package dev.daweapon.service.friend;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("dev.daweapon.service.friend.endpoints", RedisMessageHandler.class).toList();
    }
}
