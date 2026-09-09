package net.swofty.service.party;

import net.swofty.commons.ServiceType;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.List;

// Backend microservice that owns party state and processes party actions (invites, leave, kick, warp, etc.) via Redis.
public class PartyService implements SkyBlockService {

    static void main(String[] args) {
        SkyBlockService.init(new PartyService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.PARTY;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("net.swofty.service.party.endpoints", RedisMessageHandler.class).toList();
    }
}