package net.swofty.service.guild;

import net.swofty.service.generic.MongoReadiness;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("net.swofty.service.guild.endpoints", RedisMessageHandler.class).toList();
    }
}
