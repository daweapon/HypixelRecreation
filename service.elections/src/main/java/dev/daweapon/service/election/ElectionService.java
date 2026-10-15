package dev.daweapon.service.election;

import dev.daweapon.service.generic.MongoReadiness;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice for SkyBlock mayor elections: candidates, voting, tallies, and resolution, backed by MongoDB.
public class ElectionService implements SkyBlockService {

    public static void main(String[] args) {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        String mongoUri = ConfigProvider.settings().getMongodb();
        new ElectionDatabase(null).connect(mongoUri);
        SkyBlockService.init(new ElectionService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.ELECTION;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.election.endpoints", RedisMessageHandler.class).toList();
    }
}
