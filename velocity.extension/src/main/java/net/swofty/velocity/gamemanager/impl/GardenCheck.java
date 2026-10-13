package net.swofty.velocity.gamemanager.impl;

import com.velocitypowered.api.proxy.Player;
import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.proxy.from.DoesServerHaveGardenProtocol;
import net.swofty.velocity.data.ProfilesDatabase;
import net.swofty.velocity.data.UserDatabase;
import net.swofty.velocity.gamemanager.BalanceConfiguration;
import net.swofty.velocity.gamemanager.GameManager;
import net.swofty.commons.redis.RedisClient;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Balance strategy that routes a player to whichever Garden server already hosts their active profile's garden, if any.
public class GardenCheck extends BalanceConfiguration {
    private static final long LOOKUP_TIMEOUT_MILLIS = 3000;

    @Override
    public GameManager.GameServer getServer(Player player, List<GameManager.GameServer> servers) {
        Document userDatabase = new UserDatabase(player.getUniqueId()).getDocument();
        if (userDatabase == null || !userDatabase.containsKey("selected")) {
            return null;
        }

        UUID activeProfile = UUID.fromString(userDatabase.getString("selected"));
        Document document = new ProfilesDatabase(activeProfile.toString()).getDocument();
        if (document == null || !document.containsKey("island_uuid") ||
            document.getString("island_uuid").equals("null")) {
            return null;
        }

        UUID gardenUUID = UUID.fromString(document.getString("island_uuid").replace("\"", ""));
        return findServerWithGarden(gardenUUID);
    }

    private static GameManager.GameServer findServerWithGarden(UUID gardenUUID) {
        Map<GameManager.GameServer, CompletableFuture<DoesServerHaveGardenProtocol.Response>> responses = new LinkedHashMap<>();

        for (Map.Entry<ServerType, ArrayList<GameManager.GameServer>> entry : GameManager.getServers().entrySet()) {
            if (entry.getKey() != ServerType.SKYBLOCK_THE_GARDEN) continue;

            for (GameManager.GameServer gameServer : new ArrayList<>(entry.getValue())) {
                responses.put(gameServer, RedisClient.requestServer(
                    gameServer.internalID(),
                    new DoesServerHaveGardenProtocol(),
                    new DoesServerHaveGardenProtocol.Request(gardenUUID.toString())));
            }
        }

        GameManager.GameServer toSendTo = null;
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(LOOKUP_TIMEOUT_MILLIS);

        for (Map.Entry<GameManager.GameServer, CompletableFuture<DoesServerHaveGardenProtocol.Response>> entry : responses.entrySet()) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) break;

            try {
                if (entry.getValue().get(remaining, TimeUnit.NANOSECONDS).serverHasIt()) {
                    toSendTo = entry.getKey();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignoredException) {
            }
        }

        return toSendTo;
    }
}
