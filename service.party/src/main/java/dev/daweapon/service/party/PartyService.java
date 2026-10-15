package dev.daweapon.service.party;

import dev.daweapon.commons.ServiceType;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;

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
        return loopThroughPackage("dev.daweapon.service.party.endpoints", RedisMessageHandler.class).toList();
    }
}