package net.swofty.type.crystalhollows.redis;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.IsServerJoinableProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.crystalhollows.lifecycle.CrystalHollowsLobby;

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
