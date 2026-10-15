package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetGameCountsProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.orchestrator.OrchestratorCache;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that reports current player and game counts for a given server type/game type/map filter.
public class GetGameCountsEndpoint implements RedisMessageHandler
        <GetGameCountsProtocol.GetGameCountsMessage,
                GetGameCountsProtocol.GetGameCountsResponse> {

    @Override
    public RedisProtocol<GetGameCountsProtocol.GetGameCountsMessage, GetGameCountsProtocol.GetGameCountsResponse> protocol() {
        return new GetGameCountsProtocol();
    }

    @Override
    public GetGameCountsProtocol.GetGameCountsResponse handle(GetGameCountsProtocol.GetGameCountsMessage body, RedisMessageContext context) {
        var stats = OrchestratorCache.getGameCounts(body.type(), body.gameTypeName(), body.mapName());
        return new GetGameCountsProtocol.GetGameCountsResponse(stats.playerCount(), stats.gameCount(), true, null);
    }
}
