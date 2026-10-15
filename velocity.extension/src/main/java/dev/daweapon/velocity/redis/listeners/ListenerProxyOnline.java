package dev.daweapon.velocity.redis.listeners;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.ProxyIsOnlineProtocol;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.UUID;

// Redis handler that reports whether the requesting server is currently known to the proxy.
public class ListenerProxyOnline implements RedisMessageHandler<
        ProxyIsOnlineProtocol.Request,
        ProxyIsOnlineProtocol.Response> {

    @Override
    public RedisProtocol<ProxyIsOnlineProtocol.Request, ProxyIsOnlineProtocol.Response> protocol() {
        return new ProxyIsOnlineProtocol();
    }

    @Override
    public ProxyIsOnlineProtocol.Response handle(ProxyIsOnlineProtocol.Request message, RedisMessageContext context) {
        if (GameManager.getFromUUID(UUID.fromString(context.origin().id())) == null) {
            return new ProxyIsOnlineProtocol.Response(false, true, null);
        }
        return new ProxyIsOnlineProtocol.Response(true, true, null);
    }
}
