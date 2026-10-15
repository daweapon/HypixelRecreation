package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.OrchestratorStatusProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.orchestrator.OrchestratorCache;
import dev.daweapon.service.orchestrator.instance.CapacityReport;
import dev.daweapon.service.orchestrator.instance.InstancedActivities;

public class OrchestratorStatusEndpoint implements RedisMessageHandler
        <OrchestratorStatusProtocol.StatusRequest, OrchestratorStatusProtocol.StatusResponse> {

    @Override
    public RedisProtocol<OrchestratorStatusProtocol.StatusRequest, OrchestratorStatusProtocol.StatusResponse> protocol() {
        return new OrchestratorStatusProtocol();
    }

    @Override
    public OrchestratorStatusProtocol.StatusResponse handle(OrchestratorStatusProtocol.StatusRequest body, RedisMessageContext context) {
        var servers = OrchestratorCache.getAllServers();
        return new OrchestratorStatusProtocol.StatusResponse(CapacityReport.build(
                servers,
                type -> (int) OrchestratorCache.getAllActiveGames().stream()
                        .filter(game -> game.game().getType() == type).count(),
                type -> InstancedActivities.queue().length(type)));
    }
}
