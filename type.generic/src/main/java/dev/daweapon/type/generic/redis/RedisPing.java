package dev.daweapon.type.generic.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.from.PingServerProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;

public class RedisPing implements RedisMessageHandler<PingServerProtocol.Request, PingServerProtocol.Response> {
    @Override
    public RedisProtocol<PingServerProtocol.Request, PingServerProtocol.Response> protocol() {
        return new PingServerProtocol();
    }

    @Override
    public PingServerProtocol.Response handle(PingServerProtocol.Request message, RedisMessageContext context) {
        return new PingServerProtocol.Response();
    }
}
