package dev.daweapon.service.generic.redis;

import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.redis.RedisEndpoint;
import dev.daweapon.redisapi.api.RedisAPI;

// Connects a service to Redis and identifies it under its ServiceType so messages can be routed to it.
public class ServiceRedisManager {
    public static void connect(String URI, ServiceType type) {
        RedisAPI.generateInstance(URI);
        RedisAPI.getInstance().setFilterId(type.name());
        RedisClient.identify(RedisEndpoint.service(type));
    }
}
