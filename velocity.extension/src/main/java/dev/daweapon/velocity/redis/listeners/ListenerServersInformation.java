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

import org.json.JSONArray;
import org.json.JSONObject;

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
        boolean isInTestFlow = isOriginInTestFlow(context.origin().id());

        switch (requestType) {
            case "ALL" -> {
                List<GameManager.GameServer> servers = new ArrayList<>(GameManager.getServers().values().stream().flatMap(List::stream).toList());
                filterByTestFlow(servers, isInTestFlow);
                JSONObject json = UnderstandableProxyServer.toJSON(toUnderstandable(servers));
                JSONArray entries = json.getJSONArray("servers");
                for (int i = 0; i < entries.length(); i++) {
                    entries.getJSONObject(i).put("host",
                            servers.get(i).registeredServer().getServerInfo().getAddress().getHostString());
                }
                return new RequestServersProtocol.Response(json.toString(), true, null);
            }
            case "TYPE" -> {
                List<GameManager.GameServer> ofType = GameManager.getFromType(ServerType.valueOf(message.type()));
                List<GameManager.GameServer> servers = ofType == null ? new ArrayList<>() : new ArrayList<>(ofType);
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

    /**
     * Services (the control plane's service.api) ask too, and their origin is a service name
     * rather than a server UUID; parsing it as one threw and the query was never answered.
     */
    private static boolean isOriginInTestFlow(String originId) {
        try {
            return TestFlowManager.isServerInTestFlow(UUID.fromString(originId));
        } catch (IllegalArgumentException e) {
            return false;
        }
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
