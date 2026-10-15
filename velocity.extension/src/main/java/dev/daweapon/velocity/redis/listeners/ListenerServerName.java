package dev.daweapon.velocity.redis.listeners;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.RequestServerNameProtocol;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.UUID;

// Redis handler that resolves the requesting server's own display name and short display name from GameManager.
public class ListenerServerName implements RedisMessageHandler<
        RequestServerNameProtocol.Request,
        RequestServerNameProtocol.Response> {

    @Override
    public RedisProtocol<RequestServerNameProtocol.Request, RequestServerNameProtocol.Response> protocol() {
        return new RequestServerNameProtocol();
    }

    @Override
    public RequestServerNameProtocol.Response handle(RequestServerNameProtocol.Request message, RedisMessageContext context) {
        GameManager.GameServer server = GameManager.getFromUUID(UUID.fromString(context.origin().id()));
        if (server == null) {
            return new RequestServerNameProtocol.Response(null, null, false,
                    "Server " + context.origin().id() + " is not registered with the proxy");
        }

        return new RequestServerNameProtocol.Response(server.displayName(), server.shortDisplayName(), true, null);
    }
}
