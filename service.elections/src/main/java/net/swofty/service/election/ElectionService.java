package net.swofty.service.election;

import net.swofty.service.generic.MongoReadiness;
import net.swofty.commons.ServiceType;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("net.swofty.service.election.endpoints", RedisMessageHandler.class).toList();
    }
}
