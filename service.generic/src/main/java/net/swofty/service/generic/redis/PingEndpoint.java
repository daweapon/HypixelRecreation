package net.swofty.service.generic.redis;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.PingProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;

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
