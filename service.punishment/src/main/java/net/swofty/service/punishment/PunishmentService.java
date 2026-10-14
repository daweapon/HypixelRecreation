package net.swofty.service.punishment;

import net.swofty.service.generic.MongoReadiness;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.punishment.PunishmentRedis;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("net.swofty.service.punishment.endpoints", RedisMessageHandler.class).toList();
    }
}
