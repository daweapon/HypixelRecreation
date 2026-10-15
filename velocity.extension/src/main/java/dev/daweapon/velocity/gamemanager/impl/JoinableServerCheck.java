package dev.daweapon.velocity.gamemanager.impl;

import com.velocitypowered.api.proxy.Player;
import dev.daweapon.commons.protocol.objects.proxy.from.IsServerJoinableProtocol;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.velocity.gamemanager.BalanceConfiguration;
import dev.daweapon.velocity.gamemanager.GameManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Balance strategy that skips servers reporting they no longer accept new players, then picks the emptiest remaining one.
// A server that fails to answer in time is treated as joinable so a slow reply never strands players.
public final class JoinableServerCheck extends BalanceConfiguration {
    private static final long LOOKUP_TIMEOUT_MILLIS = 1500;
    private static final LowestPlayerCount FALLBACK = new LowestPlayerCount();

    @Override
    public GameManager.GameServer getServer(Player player, List<GameManager.GameServer> servers) {
        Map<GameManager.GameServer, CompletableFuture<IsServerJoinableProtocol.Response>> responses = new LinkedHashMap<>();
        for (GameManager.GameServer server : servers) {
            responses.put(server, RedisClient.requestServer(
                    server.internalID(), new IsServerJoinableProtocol(), new IsServerJoinableProtocol.Request()));
        }

        List<GameManager.GameServer> joinable = new ArrayList<>();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(LOOKUP_TIMEOUT_MILLIS);
        for (Map.Entry<GameManager.GameServer, CompletableFuture<IsServerJoinableProtocol.Response>> entry : responses.entrySet()) {
            try {
                long remaining = Math.max(0, deadline - System.nanoTime());
                if (entry.getValue().get(remaining, TimeUnit.NANOSECONDS).joinable()) joinable.add(entry.getKey());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception exception) {
                joinable.add(entry.getKey());
            }
        }
        return FALLBACK.getServer(player, joinable);
    }
}
