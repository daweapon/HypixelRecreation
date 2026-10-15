package dev.daweapon.velocity.redis.listeners;

import com.velocitypowered.api.proxy.Player;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.UnderstandableProxyServer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.RequestServersProtocol;
import dev.daweapon.velocity.SkyBlockVelocity;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.velocity.testflow.TestFlowManager;

import java.util.*;

// Redis handler that answers server-directory queries (all, by type, by UUID, or by player) filtered for test-flow isolation.
public class ListenerServersInformation implements RedisMessageHandler<
        RequestServersProtocol.Request,
        RequestServersProtocol.Response> {

    @Override
    public RedisProtocol<RequestServersProtocol.Request, RequestServersProtocol.Response> protocol() {
        return new RequestServersProtocol();
    }

    @Override
    public RequestServersProtocol.Response handle(RequestServersProtocol.Request message, RedisMessageContext context) {
        String requestType = message.requestType();
        boolean isInTestFlow = TestFlowManager.isServerInTestFlow(UUID.fromString(context.origin().id()));

        switch (requestType) {
            case "ALL" -> {
                List<GameManager.GameServer> servers = new ArrayList<>(GameManager.getServers().values().stream().flatMap(List::stream).toList());
                filterByTestFlow(servers, isInTestFlow);
                return new RequestServersProtocol.Response(UnderstandableProxyServer.toJSON(toUnderstandable(servers)).toString(), true, null);
            }
            case "TYPE" -> {
                List<GameManager.GameServer> servers = GameManager.getFromType(ServerType.valueOf(message.type()));
                if (servers == null) servers = new ArrayList<>();
                filterByTestFlow(servers, isInTestFlow);
                return new RequestServersProtocol.Response(UnderstandableProxyServer.toJSON(toUnderstandable(servers)).toString(), true, null);
            }
            case "UUID" -> {
                List<GameManager.GameServer> servers = new ArrayList<>(List.of(GameManager.getFromUUID(UUID.fromString(message.uuid()))));
                filterByTestFlow(servers, isInTestFlow);
                return new RequestServersProtocol.Response(UnderstandableProxyServer.toJSON(toUnderstandable(servers)).toString(), true, null);
            }
            case "PLAYER_UUID" -> {
                UUID uuidObject = UUID.fromString(message.uuid());
                Optional<Player> potentialPlayer = SkyBlockVelocity.getServer().getPlayer(uuidObject);
                if (potentialPlayer.isEmpty()) {
                    return new RequestServersProtocol.Response(UnderstandableProxyServer.toJSON(List.of()).toString(), true, null);
                }
                Player player = potentialPlayer.get();
                List<GameManager.GameServer> servers = new ArrayList<>(Collections.singletonList(GameManager.getFromRegisteredServer(player.getCurrentServer().get().getServer())));
                filterByTestFlow(servers, isInTestFlow);
                return new RequestServersProtocol.Response(UnderstandableProxyServer.toJSON(toUnderstandable(servers)).toString(), true, null);
            }
        }

        throw new RuntimeException("Unknown request type: " + requestType);
    }

    private void filterByTestFlow(List<GameManager.GameServer> servers, boolean isInTestFlow) {
        if (isInTestFlow) {
            servers.removeIf(server -> {
                TestFlowManager.ProxyTestFlowInstance testFlowInstance = TestFlowManager.getFromServerUUID(server.internalID());
                return testFlowInstance == null || !testFlowInstance.hasServer(server.internalID());
            });
        } else {
            servers.removeIf(server -> {
                TestFlowManager.ProxyTestFlowInstance testFlowInstance = TestFlowManager.getFromServerUUID(server.internalID());
                return testFlowInstance != null;
            });
        }
    }

    private List<UnderstandableProxyServer> toUnderstandable(List<GameManager.GameServer> servers) {
        return servers.stream().map(server -> new UnderstandableProxyServer(
                server.displayName(),
                server.internalID(),
                GameManager.getTypeFromRegisteredServer(server.registeredServer()),
                server.registeredServer().getServerInfo().getAddress().getPort(),
                server.registeredServer().getPlayersConnected().stream().map(Player::getUniqueId).toList(),
                server.maxPlayers(),
                server.shortDisplayName()
        )).toList();
    }
}
