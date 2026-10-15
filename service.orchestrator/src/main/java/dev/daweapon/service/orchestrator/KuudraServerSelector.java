package dev.daweapon.service.orchestrator;

import dev.daweapon.commons.ServerType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.ToIntFunction;

public final class KuudraServerSelector {
    private KuudraServerSelector() {
    }

    public static List<OrchestratorCache.GameServerState> rank(Collection<OrchestratorCache.GameServerState> servers,
                                                               int neededSlots,
                                                               ToIntFunction<UUID> reservedPlayerSlots) {
        return InstancedServerSelector.rank(ServerType.SKYBLOCK_KUUDRA, servers, neededSlots, reservedPlayerSlots);
    }
}
