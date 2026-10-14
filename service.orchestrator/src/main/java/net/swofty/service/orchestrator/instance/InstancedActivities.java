package net.swofty.service.orchestrator.instance;

import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol;
import net.swofty.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.service.orchestrator.KuudraServerSelector;
import net.swofty.service.orchestrator.OrchestratorCache;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class InstancedActivities {
    private static final long START_TIMEOUT_SECONDS = 15;
    private static final Duration KUUDRA_MAX_WAIT = Duration.ofSeconds(45);
    private static final Duration RAVENGARD_MAX_WAIT = Duration.ofSeconds(10);
    private static final ActivityQueue QUEUE = new ActivityQueue();
    private static final InstanceAllocator ALLOCATOR = new InstanceAllocator(
            (server, request) -> RedisClient.requestServerFromService(
                    server.uuid(), new InstantiateGamePushProtocol(), request).get(START_TIMEOUT_SECONDS, TimeUnit.SECONDS),
            (server, slots) -> OrchestratorCache.reservePlayerSlots(server.uuid(), slots));

    private InstancedActivities() {}

    public static ActivityQueue queue() {
        return QUEUE;
    }

    public static GetServerForMapProtocol.GetServerForMapResponse kuudra(GetServerForMapProtocol.GetServerForMapMessage body) {
        String tier = body.mode();
        if (tier == null || tier.isBlank()) {
            return new GetServerForMapProtocol.GetServerForMapResponse(null, null, true, "No Kuudra tier given");
        }
        int neededSlots = Math.max(1, body.neededSlots());
        InstantiateGamePushProtocol.Request request = new InstantiateGamePushProtocol.Request(tier, body.map());

        Allocation allocation = QUEUE.await(ServerType.SKYBLOCK_KUUDRA, KUUDRA_MAX_WAIT, () -> {
            List<OrchestratorCache.GameServerState> candidates = KuudraServerSelector.rank(
                    OrchestratorCache.getAllServers(), neededSlots, OrchestratorCache::getReservedPlayerSlots);
            return ALLOCATOR.allocate(candidates, anyOnline(ServerType.SKYBLOCK_KUUDRA), request, neededSlots);
        });
        Logger.info("[matchmaking] SKYBLOCK_KUUDRA tier={} party={} -> {}", tier, neededSlots, describe(allocation));
        return toResponse(allocation, Messages.KUUDRA);
    }

    public static GetServerForMapProtocol.GetServerForMapResponse ravengardNew(String mode, String map) {
        InstantiateGamePushProtocol.Request request = new InstantiateGamePushProtocol.Request(mode, map);
        Allocation allocation = QUEUE.await(ServerType.RAVENGARD_DUNGEON, RAVENGARD_MAX_WAIT, () -> {
            OrchestratorCache.GameServerState server = OrchestratorCache.pickServerForNewGame(ServerType.RAVENGARD_DUNGEON);
            List<OrchestratorCache.GameServerState> candidates = server == null ? List.of() : List.of(server);
            return ALLOCATOR.allocate(candidates, anyOnline(ServerType.RAVENGARD_DUNGEON), request, 0);
        });
        Logger.info("[matchmaking] RAVENGARD_DUNGEON mode='{}' -> {}", mode, describe(allocation));
        return toResponse(allocation, Messages.RAVENGARD);
    }

    public static UnderstandableProxyServer toProxyServer(OrchestratorCache.GameServerState server) {
        return new UnderstandableProxyServer(
                server.shortName(),
                server.uuid(),
                server.type(),
                -1,
                new ArrayList<>(),
                server.maxPlayers(),
                server.shortName());
    }

    static GetServerForMapProtocol.GetServerForMapResponse toResponse(Allocation allocation, Messages messages) {
        if (allocation.succeeded()) {
            return new GetServerForMapProtocol.GetServerForMapResponse(
                    toProxyServer(allocation.server()), allocation.gameId(), true, null);
        }
        return new GetServerForMapProtocol.GetServerForMapResponse(
                null, null, true, messages.forReason(allocation.failure()), allocation.failure().name());
    }

    private static boolean anyOnline(ServerType type) {
        return OrchestratorCache.getAllServers().stream().anyMatch(server -> server.type() == type);
    }

    private static String describe(Allocation allocation) {
        return allocation.succeeded()
                ? allocation.server().shortName()
                : allocation.failure() + (allocation.detail() == null ? "" : " (" + allocation.detail() + ")");
    }

    record Messages(String noServer, String full, String couldNotStart) {
        static final Messages KUUDRA = new Messages("No Kuudra server is online", "No Kuudra server has room",
                "No Kuudra server could start the run");
        static final Messages RAVENGARD = new Messages("No dungeon servers online", "No dungeon server has room",
                "No dungeon server could start the instance");

        String forReason(FailureReason reason) {
            return switch (reason) {
                case NO_SERVER -> noServer;
                case FULL -> full;
                case TIMEOUT, REFUSED -> couldNotStart;
            };
        }
    }
}
