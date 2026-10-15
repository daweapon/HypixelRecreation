package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetMapsProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.orchestrator.OrchestratorCache;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that lists the maps currently available for a given server type/mode.
public class GetMapsEndpoint implements RedisMessageHandler
        <GetMapsProtocol.GetMapsMessage,
                GetMapsProtocol.GetMapsResponse> {

    @Override
    public RedisProtocol<GetMapsProtocol.GetMapsMessage, GetMapsProtocol.GetMapsResponse> protocol() {
        return new GetMapsProtocol();
    }

    @Override
    public GetMapsProtocol.GetMapsResponse handle(GetMapsProtocol.GetMapsMessage body, RedisMessageContext context) {
        var maps = OrchestratorCache.getMaps(body.type(), body.mode());
        return new GetMapsProtocol.GetMapsResponse(maps.stream().sorted().toList(), true, null);
    }
}