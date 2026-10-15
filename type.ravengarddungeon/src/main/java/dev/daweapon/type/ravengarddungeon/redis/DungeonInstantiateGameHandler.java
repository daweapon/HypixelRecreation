package dev.daweapon.type.ravengarddungeon.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Request;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.ravengarddungeon.game.DungeonInstanceRegistry;

// Redis service endpoint that creates a new dungeon instance in response to an orchestrator instantiate-game request.
public class DungeonInstantiateGameHandler implements RedisMessageHandler<Request, Response> {

    private static final InstantiateGamePushProtocol PROTOCOL = new InstantiateGamePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request request, RedisMessageContext context) {
        try {
            DungeonInstanceRegistry.DungeonInstance instance =
                    DungeonInstanceRegistry.create(request.gameType());
            return Response.success(instance.getGameId().toString(), "generated", instance.getMode());
        } catch (Exception exception) {
            return Response.failure("Failed to instantiate dungeon: " + exception.getMessage());
        }
    }
}
