package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.OrchestratorQueueProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.orchestrator.instance.InstancedActivities;

public class OrchestratorQueueEndpoint implements RedisMessageHandler
        <OrchestratorQueueProtocol.QueueRequest, OrchestratorQueueProtocol.QueueResponse> {

    @Override
    public RedisProtocol<OrchestratorQueueProtocol.QueueRequest, OrchestratorQueueProtocol.QueueResponse> protocol() {
        return new OrchestratorQueueProtocol();
    }

    @Override
    public OrchestratorQueueProtocol.QueueResponse handle(OrchestratorQueueProtocol.QueueRequest body, RedisMessageContext context) {
        return new OrchestratorQueueProtocol.QueueResponse(InstancedActivities.queue().snapshot());
    }
}
