package dev.daweapon.service.orchestrator.instance;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.objects.game.QueuePositionPushProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.redis.RedisEndpointType;
import dev.daweapon.commons.redis.RedisMessageContext;
import org.tinylog.Logger;

import java.util.UUID;

public final class QueuePositionNotifier {
    private static final QueuePositionPushProtocol PROTOCOL = new QueuePositionPushProtocol();

    private QueuePositionNotifier() {
    }

    public static ActivityQueue.Waiter waiterFor(GetServerForMapProtocol.GetServerForMapMessage body, RedisMessageContext context) {
        String origin = context != null && context.origin() != null
                && context.origin().type() == RedisEndpointType.SERVER ? context.origin().id() : null;
        return new ActivityQueue.Waiter(origin, body.mode(), body.players());
    }

    public static String activityName(ServerType type) {
        return switch (type) {
            case SKYBLOCK_KUUDRA -> "Kuudra";
            case SKYBLOCK_CATACOMBS -> "The Catacombs";
            default -> type.name();
        };
    }

    static void moved(ServerType type, ActivityQueue.Waiter waiter, int position, int length) {
        if (position <= 0 || waiter.originServer() == null || waiter.players().isEmpty()) return;
        UUID server;
        try {
            server = UUID.fromString(waiter.originServer());
        } catch (IllegalArgumentException notAServer) {
            return;
        }
        RedisClient.requestServerFromService(server, PROTOCOL,
                        new QueuePositionPushProtocol.Request(waiter.players(), type, activityName(type), position, length))
                .exceptionally(error -> {
                    Logger.debug("Queue position push to {} went unanswered: {}", server, error.getMessage());
                    return null;
                });
    }
}
