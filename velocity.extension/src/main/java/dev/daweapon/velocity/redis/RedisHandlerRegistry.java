package dev.daweapon.velocity.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.redis.RedisChannels;
import dev.daweapon.commons.redis.RedisEndpoint;
import dev.daweapon.commons.redis.RedisMessageBus;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Registers a proxy-side RedisMessageHandler onto the message bus under the proxy endpoint.
public final class RedisHandlerRegistry {

    public static <T, R> void register(RedisMessageHandler<T, R> handler) {
        RedisProtocol<T, R> protocol = handler.protocol();

        RedisMessageBus.registerHandler(
                RedisEndpoint.proxy(),
                RedisChannels.protocol(protocol),
                handler,
                (envelope, channel) -> RedisMessageContext.between(
                        UUID.fromString(envelope.id()),
                        RedisEndpoint.server(envelope.from()),
                        RedisEndpoint.proxy(),
                        protocol.channel()
                ),
                envelope -> envelope.from(),
                envelope -> RedisChannels.protocol(protocol)
        );
    }
}
