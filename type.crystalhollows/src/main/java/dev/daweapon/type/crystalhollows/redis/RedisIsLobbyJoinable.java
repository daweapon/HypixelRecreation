package dev.daweapon.type.crystalhollows.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.from.IsServerJoinableProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.crystalhollows.lifecycle.CrystalHollowsLobby;

public class RedisIsLobbyJoinable implements RedisMessageHandler<IsServerJoinableProtocol.Request, IsServerJoinableProtocol.Response> {
    @Override
    public RedisProtocol<IsServerJoinableProtocol.Request, IsServerJoinableProtocol.Response> protocol() {
        return new IsServerJoinableProtocol();
    }

    @Override
    public IsServerJoinableProtocol.Response handle(IsServerJoinableProtocol.Request message, RedisMessageContext context) {
        return new IsServerJoinableProtocol.Response(CrystalHollowsLobby.acceptsNewPlayers(), true, null);
    }
}
