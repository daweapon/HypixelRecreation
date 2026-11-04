package dev.daweapon.type.generic.redis.service;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.metrics.DrainDestination;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.DrainServerPushProtocol;
import dev.daweapon.commons.protocol.objects.game.DrainServerPushProtocol.Request;
import dev.daweapon.commons.protocol.objects.game.DrainServerPushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.MinecraftServer;
import org.tinylog.Logger;

import java.util.List;

public class DrainServerHandler implements RedisMessageHandler<Request, Response> {
    private static final DrainServerPushProtocol PROTOCOL = new DrainServerPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        ServerType destination = DrainDestination.of(HypixelConst.getTypeLoader().getType());
        List<HypixelPlayer> players = HypixelGenericLoader.getLoadedPlayers();
        String reason = message.reason() == null || message.reason().isBlank() ? "Scheduled Reboot" : message.reason();
        Logger.warn("Draining {} player(s) to {} before a restart ({})", players.size(), destination, reason);
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> players.forEach(player -> {
            if (!player.isOnline()) return;
            player.sendMessage("<c>[Important] <e>This server will restart soon: <b>" + reason);
            player.sendMessage("<7>Sending you to a different server...");
            player.sendTo(destination, true);
        }));
        return new Response(players.size(), destination.name());
    }
}
