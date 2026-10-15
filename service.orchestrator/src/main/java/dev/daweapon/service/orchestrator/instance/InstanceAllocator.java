package dev.daweapon.service.orchestrator.instance;

import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol;
import dev.daweapon.service.orchestrator.OrchestratorCache;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public final class InstanceAllocator {
    @FunctionalInterface
    public interface Starter {
        InstantiateGamePushProtocol.Response start(OrchestratorCache.GameServerState server,
                                                   InstantiateGamePushProtocol.Request request) throws Exception;
    }

    private final Starter starter;
    private final BiConsumer<OrchestratorCache.GameServerState, Integer> reserver;

    public InstanceAllocator(Starter starter, BiConsumer<OrchestratorCache.GameServerState, Integer> reserver) {
        this.starter = starter;
        this.reserver = reserver;
    }

    public Allocation allocate(List<OrchestratorCache.GameServerState> candidates,
                               boolean anyServerOnline,
                               InstantiateGamePushProtocol.Request request,
                               int neededSlots) {
        if (candidates.isEmpty()) {
            return anyServerOnline
                    ? Allocation.failure(FailureReason.FULL, "Every server is full")
                    : Allocation.failure(FailureReason.NO_SERVER, "No server is online");
        }

        Allocation lastFailure = null;
        for (OrchestratorCache.GameServerState server : candidates) {
            try {
                InstantiateGamePushProtocol.Response response = starter.start(server, request);
                if (response != null && response.success() && response.gameId() != null) {
                    reserver.accept(server, neededSlots);
                    return Allocation.success(server, response.gameId());
                }
                String error = response == null ? "no response" : response.error();
                Logger.warn("[matchmaking] {} refused the instance: {}", server.shortName(), error);
                lastFailure = Allocation.failure(FailureReason.REFUSED, error);
            } catch (TimeoutException e) {
                Logger.warn("[matchmaking] {} did not answer in time", server.shortName());
                lastFailure = Allocation.failure(FailureReason.TIMEOUT, "Timed out on " + server.shortName());
            } catch (Exception e) {
                Logger.error(e, "[matchmaking] failed to start an instance on {}", server.shortName());
                lastFailure = Allocation.failure(FailureReason.REFUSED, e.getMessage());
            }
        }
        return lastFailure;
    }
}
