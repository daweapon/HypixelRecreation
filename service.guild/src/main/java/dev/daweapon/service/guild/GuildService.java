package dev.daweapon.service.guild;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice that owns guild state and processes guild-related requests via Redis.
public class GuildService implements SkyBlockService {

    static void main() {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        String mongoUri = ConfigProvider.settings().getMongodb();
        new GuildDatabase(null).connect(mongoUri);

        SkyBlockService.init(new GuildService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.GUILD;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.guild.endpoints", RedisMessageHandler.class).toList();
    }
}
