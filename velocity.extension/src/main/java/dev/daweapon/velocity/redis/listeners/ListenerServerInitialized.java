package dev.daweapon.velocity.redis.listeners;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.RegisterServerProtocol;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.UUID;

// Redis handler that registers a newly-started backend server with GameManager and returns its assigned host/port.
public class ListenerServerInitialized implements RedisMessageHandler<
        RegisterServerProtocol.Request,
        RegisterServerProtocol.Response> {

    @Override
    public RedisProtocol<RegisterServerProtocol.Request, RegisterServerProtocol.Response> protocol() {
        return new RegisterServerProtocol();
    }

    @Override
    public RegisterServerProtocol.Response handle(RegisterServerProtocol.Request message, RedisMessageContext context) {
        ServerType type = ServerType.valueOf(message.type());
        int port = message.port() != null ? message.port() : -1;

        String host = message.host() != null
                ? message.host()
                : ConfigProvider.settings().getHostName();

        int maxPlayers = message.maxPlayers();

        UUID serverID = UUID.fromString(context.origin().id());
        GameManager.GameServer server = GameManager.getFromUUID(serverID);
        if (server == null) {
            server = GameManager.addServer(type, serverID, host, port, maxPlayers);
        }

        return new RegisterServerProtocol.Response(
                server.registeredServer().getServerInfo().getAddress().getHostString(),
                server.registeredServer().getServerInfo().getAddress().getPort(),
                true,
                null);
    }
}
