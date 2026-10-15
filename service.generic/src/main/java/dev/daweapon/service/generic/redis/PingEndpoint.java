package dev.daweapon.service.generic.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.PingProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

// Trivial Redis endpoint every service registers so callers can health-check that it is alive and responding.
public class PingEndpoint implements RedisMessageHandler<
        PingProtocol.EmptyMessage,
        PingProtocol.EmptyMessage> {
    @Override
    public RedisProtocol<PingProtocol.EmptyMessage, PingProtocol.EmptyMessage> protocol() {
        return new PingProtocol();
    }

    @Override
    public PingProtocol.EmptyMessage handle(PingProtocol.EmptyMessage messageObject, RedisMessageContext context) {
        return new PingProtocol.EmptyMessage();
    }
}
