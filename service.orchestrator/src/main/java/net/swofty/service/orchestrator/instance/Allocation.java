package net.swofty.service.orchestrator.instance;

import net.swofty.service.orchestrator.OrchestratorCache;
import org.jetbrains.annotations.Nullable;

public record Allocation(@Nullable OrchestratorCache.GameServerState server,
                         @Nullable String gameId,
                         @Nullable FailureReason failure,
                         @Nullable String detail) {

    public static Allocation success(OrchestratorCache.GameServerState server, String gameId) {
        return new Allocation(server, gameId, null, null);
    }

    public static Allocation failure(FailureReason reason, @Nullable String detail) {
        return new Allocation(null, null, reason, detail);
    }

    public boolean succeeded() {
        return failure == null;
    }
}
