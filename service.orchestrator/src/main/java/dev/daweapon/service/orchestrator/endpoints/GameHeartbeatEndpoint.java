package dev.daweapon.service.orchestrator.endpoints;

import dev.daweapon.service.orchestrator.instance.InstancedActivities;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.orchestrator.OrchestratorCache;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that receives a game server's periodic heartbeat and updates the orchestrator's tracked state for it.
public class GameHeartbeatEndpoint implements RedisMessageHandler
        <GameHeartbeatProtocol.HeartbeatMessage,
                GameHeartbeatProtocol.HeartbeatResponse> {

    @Override
    public RedisProtocol<GameHeartbeatProtocol.HeartbeatMessage, GameHeartbeatProtocol.HeartbeatResponse> protocol() {
        return new GameHeartbeatProtocol();
    }

    @Override
    public GameHeartbeatProtocol.HeartbeatResponse handle(GameHeartbeatProtocol.HeartbeatMessage body, RedisMessageContext context) {
        OrchestratorCache.handleHeartbeat(
            body.uuid(),
            body.shortName(),
            body.type(),
            body.maxPlayers(),
            body.onlinePlayers(),
            body.games(),
            body.mapAdvertisements(),
            body.remainingGameSlots()
        );
        InstancedActivities.queue().capacityMayHaveChanged();
        return new GameHeartbeatProtocol.HeartbeatResponse(true, null);
    }
}