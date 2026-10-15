package dev.daweapon.type.generic.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.from.PlayerRankProtocol;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.UUID;

public class RedisPlayerRank implements RedisMessageHandler<PlayerRankProtocol.Request, PlayerRankProtocol.Response> {
    @Override
    public RedisProtocol<PlayerRankProtocol.Request, PlayerRankProtocol.Response> protocol() {
        return new PlayerRankProtocol();
    }

    @Override
    public PlayerRankProtocol.Response handle(PlayerRankProtocol.Request message, RedisMessageContext context) {
        UUID uuid = UUID.fromString(message.uuid());
        HypixelPlayer player = HypixelGenericLoader.getFromUUID(uuid);
        if (player == null) return new PlayerRankProtocol.Response(false, null);

        return new PlayerRankProtocol.Response(true, player.getRank().name());
    }
}
