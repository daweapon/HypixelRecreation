package net.swofty.service.orchestrator.instance;

import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.orchestrator.OrchestratorStatusProtocol.TypeStatus;
import net.swofty.service.orchestrator.OrchestratorCache;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public final class CapacityReport {
    private CapacityReport() {}

    public static List<TypeStatus> build(Collection<OrchestratorCache.GameServerState> servers,
                                         ToIntFunction<ServerType> instancesOf,
                                         ToIntFunction<ServerType> queuedOf) {
        Map<ServerType, List<OrchestratorCache.GameServerState>> byType = servers.stream()
                .collect(Collectors.groupingBy(OrchestratorCache.GameServerState::type));
        return byType.entrySet().stream()
                .map(entry -> {
                    List<OrchestratorCache.GameServerState> group = entry.getValue();
                    int freeSlots = group.stream()
                            .filter(server -> server.remainingGameSlots() != null)
                            .mapToInt(OrchestratorCache.GameServerState::remainingGameSlots)
                            .sum();
                    return new TypeStatus(entry.getKey(), group.size(), instancesOf.applyAsInt(entry.getKey()), freeSlots,
                            group.stream().mapToInt(OrchestratorCache.GameServerState::onlinePlayers).sum(),
                            group.stream().mapToInt(OrchestratorCache.GameServerState::maxPlayers).sum(),
                            queuedOf.applyAsInt(entry.getKey()));
                })
                .sorted(Comparator.comparing(status -> status.type().name()))
                .toList();
    }
}
