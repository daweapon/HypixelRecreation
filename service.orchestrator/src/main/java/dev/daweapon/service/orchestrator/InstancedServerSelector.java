package dev.daweapon.service.orchestrator;

import dev.daweapon.commons.ServerType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * Picks the servers of one type that can host a new private instance for a party: a free instance slot and room
 * for every member (after players already on their way), best first.
 */
public final class InstancedServerSelector {
    private InstancedServerSelector() {
    }

    public static List<OrchestratorCache.GameServerState> rank(ServerType type,
                                                               Collection<OrchestratorCache.GameServerState> servers,
                                                               int neededSlots,
                                                               ToIntFunction<UUID> reservedPlayerSlots) {
        return servers.stream()
                .filter(server -> server.type() == type)
                .filter(server -> freeRuns(server) > 0)
                .filter(server -> freePlayers(server, reservedPlayerSlots) >= neededSlots)
                .sorted(Comparator
                        .comparingInt(InstancedServerSelector::freeRuns)
                        .thenComparingInt(server -> freePlayers(server, reservedPlayerSlots))
                        .reversed())
                .toList();
    }

    private static int freeRuns(OrchestratorCache.GameServerState server) {
        return server.remainingGameSlots() == null ? Integer.MAX_VALUE : server.remainingGameSlots();
    }

    private static int freePlayers(OrchestratorCache.GameServerState server, ToIntFunction<UUID> reservedPlayerSlots) {
        return server.availableSlots() - reservedPlayerSlots.applyAsInt(server.uuid());
    }
}
