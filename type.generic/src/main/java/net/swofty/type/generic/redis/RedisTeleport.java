package net.swofty.type.generic.redis;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.TeleportProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.arrival.PendingArrivals;

import java.util.UUID;
import net.swofty.commons.redis.RedisMessageContext;

public class RedisTeleport implements RedisMessageHandler<TeleportProtocol.Request, TeleportProtocol.Response> {
    @Override
    public RedisProtocol<TeleportProtocol.Request, TeleportProtocol.Response> protocol() {
        return new TeleportProtocol();
    }

    @Override
    public TeleportProtocol.Response handle(TeleportProtocol.Request message, RedisMessageContext context) {
        UUID uuid = UUID.fromString(message.uuid());
        HypixelPlayer player = HypixelGenericLoader.getFromUUID(uuid);
        Pos pos = new Pos(message.x(), message.y(), message.z(), message.yaw(), message.pitch());
        if (player == null) {
            if (message.instanceId() == null) PendingArrivals.get().expect(uuid, pos);
            return new TeleportProtocol.Response();
        }

        Instance targetInstance = message.instanceId() != null
                ? MinecraftServer.getInstanceManager().getInstance(UUID.fromString(message.instanceId()))
                : null;

        if (targetInstance != null && targetInstance != player.getInstance()) {
            player.setInstance(targetInstance, pos);
        } else {
            player.teleport(pos);
        }
        return new TeleportProtocol.Response();
    }
}
