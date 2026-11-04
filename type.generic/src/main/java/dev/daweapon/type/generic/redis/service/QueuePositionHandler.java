package dev.daweapon.type.generic.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.QueuePositionPushProtocol;
import dev.daweapon.commons.protocol.objects.game.QueuePositionPushProtocol.Request;
import dev.daweapon.commons.protocol.objects.game.QueuePositionPushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.queue.QueuePositions;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.UUID;

public class QueuePositionHandler implements RedisMessageHandler<Request, Response> {
    private static final QueuePositionPushProtocol PROTOCOL = new QueuePositionPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        int delivered = 0;
        for (UUID uuid : message.players()) {
            HypixelPlayer player = HypixelGenericLoader.getFromUUID(uuid);
            if (player == null || !player.isOnline()) continue;
            if (QueuePositions.update(uuid, message.activity(), message.position(), message.length())) {
                player.sendMessage(QueuePositions.chatLine(message.activity(), message.position()));
            }
            delivered++;
        }
        return new Response(delivered);
    }
}
