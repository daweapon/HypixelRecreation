package net.swofty.type.generic.redis;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.PlayerLocationProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.UUID;
import net.swofty.commons.redis.RedisMessageContext;

public class RedisPlayerLocation implements RedisMessageHandler<PlayerLocationProtocol.Request, PlayerLocationProtocol.Response> {
    @Override
    public RedisProtocol<PlayerLocationProtocol.Request, PlayerLocationProtocol.Response> protocol() {
        return new PlayerLocationProtocol();
    }

    @Override
    public PlayerLocationProtocol.Response handle(PlayerLocationProtocol.Request message, RedisMessageContext context) {
        UUID uuid = UUID.fromString(message.uuid());
        HypixelPlayer player = HypixelGenericLoader.getFromUUID(uuid);
        if (player == null) return new PlayerLocationProtocol.Response(false, 0, 0, 0, 0, 0, null);

        Pos pos = player.getPosition();
        String instanceId = player.getInstance() != null ? player.getInstance().getUuid().toString() : null;
        return new PlayerLocationProtocol.Response(true, pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch(), instanceId);
    }
}
