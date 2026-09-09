package net.swofty.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.proxy.from.PlayerLocationProtocol;
import net.swofty.commons.protocol.objects.proxy.from.PlayerRankProtocol;
import net.swofty.commons.protocol.objects.proxy.from.TeleportProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import net.swofty.velocity.SkyBlockVelocity;
import net.swofty.velocity.gamemanager.GameManager;

import java.util.UUID;

// Staff-only "/find" command that locates a player's current server and optionally teleports the sender to them.
public class FindCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) {
            invocation.source().sendMessage(Text.of("<c>Only players can use this command."));
            return;
        }

        RegisteredServer senderServer = sender.getCurrentServer().map(conn -> conn.getServer()).orElse(null);
        if (senderServer == null) {
            sender.sendMessage(Text.of("<c>Unable to verify your rank right now."));
            return;
        }

        UUID senderServerUUID = UUID.fromString(senderServer.getServerInfo().getName());
        RedisClient.requestServer(senderServerUUID,
                new PlayerRankProtocol(),
                new PlayerRankProtocol.Request(sender.getUniqueId().toString())
        ).thenAccept(rankResponse -> {
            if (rankResponse == null || !rankResponse.found() || !"STAFF".equalsIgnoreCase(rankResponse.rank())) {
                sender.sendMessage(Text.of("<c>You do not have permission to use this command."));
                return;
            }

            runFind(sender, invocation.arguments());
        });
    }

    private void runFind(Player sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Text.of("<c>Usage: /find <username> [tp]"));
            return;
        }

        String username = args[0];
        boolean shouldTeleport = args.length > 1 && args[1].equalsIgnoreCase("tp");

        SkyBlockVelocity.getServer().getPlayer(username).ifPresentOrElse(target -> {
            RegisteredServer targetServer = target.getCurrentServer()
                    .map(conn -> conn.getServer())
                    .orElse(null);

            if (targetServer == null) {
                sender.sendMessage(Text.of("<c>{} is not currently on any server.", username));
                return;
            }

            ServerType type = GameManager.getTypeFromRegisteredServer(targetServer);
            GameManager.GameServer gameServer = GameManager.getFromRegisteredServer(targetServer);
            String displayName = gameServer != null ? gameServer.displayName() : targetServer.getServerInfo().getName();

            if (!shouldTeleport) {
                sender.sendMessage(Text.of("<a>{} <7>is on <a>{} <7>({}).",
                        target.getUsername(), type != null ? type.name() : "UNKNOWN", displayName));
                sender.sendMessage(Text.of("<7>Run <a>/find {} tp <7>to teleport to them.", username));
                return;
            }

            UUID targetServerUUID = UUID.fromString(targetServer.getServerInfo().getName());
            RedisClient.requestServer(targetServerUUID,
                    new PlayerLocationProtocol(),
                    new PlayerLocationProtocol.Request(target.getUniqueId().toString())
            ).thenAccept(location -> {
                if (location == null || !location.found()) {
                    sender.sendMessage(Text.of("<c>Could not locate {}'s exact position.", username));
                    return;
                }

                boolean sameServer = sender.getCurrentServer()
                        .map(conn -> conn.getServer().equals(targetServer))
                        .orElse(false);

                if (sameServer) {
                    RedisClient.requestServer(targetServerUUID,
                            new TeleportProtocol(),
                            new TeleportProtocol.Request(sender.getUniqueId().toString(),
                                    location.x(), location.y(), location.z(), location.yaw(), location.pitch(),
                                    location.instanceId()));
                    sender.sendMessage(Text.of("<a>Teleported to {}.", username));
                } else {
                    sender.createConnectionRequest(targetServer).connectWithIndication().thenAccept(result -> {
                        if (result == null || !result) return;
                        RedisClient.requestServer(targetServerUUID,
                                new TeleportProtocol(),
                                new TeleportProtocol.Request(sender.getUniqueId().toString(),
                                        location.x(), location.y(), location.z(), location.yaw(), location.pitch(),
                                        location.instanceId()));
                        sender.sendMessage(Text.of("<a>Teleported to {}.", username));
                    });
                }
            });
        }, () -> sender.sendMessage(Text.of("<c>Could not find player {}.", username)));
    }
}
