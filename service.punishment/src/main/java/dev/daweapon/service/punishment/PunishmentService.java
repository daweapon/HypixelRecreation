package dev.daweapon.service.punishment;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.punishment.PunishmentRedis;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice that issues, queries, and revokes player punishments (bans/mutes), backed by MongoDB and Redis.
public class PunishmentService implements SkyBlockService {

    static void main(String[] args) {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        String mongoUri = ConfigProvider.settings().getMongodb();
        new PunishmentDatabase(null).connect(mongoUri);
        PunishmentRedis.connect(ConfigProvider.settings().getRedisUri());
        SkyBlockService.init(new PunishmentService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.PUNISHMENT;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.punishment.endpoints", RedisMessageHandler.class).toList();
    }
}
