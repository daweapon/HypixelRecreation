package net.swofty.service.orchestrator.endpoints;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.orchestrator.OrchestratorStatusProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.service.orchestrator.OrchestratorCache;
import net.swofty.service.orchestrator.instance.CapacityReport;
import net.swofty.service.orchestrator.instance.InstancedActivities;

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
