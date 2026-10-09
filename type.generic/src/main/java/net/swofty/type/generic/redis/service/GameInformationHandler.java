package net.swofty.type.generic.redis.service;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.game.GameInformationPushProtocol;
import net.swofty.commons.protocol.objects.game.GameInformationPushProtocol.Request;
import net.swofty.commons.protocol.objects.game.GameInformationPushProtocol.Response;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.swofty.commons.redis.RedisMessageContext;

public class GameInformationHandler implements RedisMessageHandler<Request, Response> {

    public static final Map<UUID, String> game = new ConcurrentHashMap<>();

    private static final GameInformationPushProtocol PROTOCOL = new GameInformationPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        if (message.uuid() != null && message.gameId() != null) {
            game.put(message.uuid(), message.gameId());
        }
        return new Response();
    }
}
