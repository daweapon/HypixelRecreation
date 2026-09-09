package net.swofty.commons.redis;

import net.swofty.commons.protocol.RedisProtocol;

// Contract for handling one Redis RPC protocol's requests and producing a response.
public interface RedisMessageHandler<T, R> {
    RedisProtocol<T, R> protocol();

    R handle(T message, RedisMessageContext context);
}
