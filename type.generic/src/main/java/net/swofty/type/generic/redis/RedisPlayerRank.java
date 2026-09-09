package net.swofty.type.generic.redis;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.PlayerRankProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;

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
