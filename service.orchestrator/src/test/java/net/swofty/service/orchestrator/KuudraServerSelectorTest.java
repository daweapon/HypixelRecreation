package net.swofty.service.orchestrator;

import net.swofty.commons.ServerType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraServerSelectorTest {

    private static OrchestratorCache.GameServerState server(String name, ServerType type, int maxPlayers, int online,
                                                            Integer freeRuns) {
        return new OrchestratorCache.GameServerState(UUID.nameUUIDFromBytes(name.getBytes()), name, type, maxPlayers,
                online, System.currentTimeMillis(), List.of(), freeRuns);
    }

    private static List<String> names(List<OrchestratorCache.GameServerState> servers) {
        return servers.stream().map(OrchestratorCache.GameServerState::shortName).toList();
    }

    @Test
    void onlyKuudraServersWithAFreeRunAndRoomForThePartyQualify() {
        List<OrchestratorCache.GameServerState> servers = List.of(
                server("crimson", ServerType.SKYBLOCK_CRIMSON_ISLE, 100, 0, null),
                server("full-runs", ServerType.SKYBLOCK_KUUDRA, 100, 0, 0),
                server("full-players", ServerType.SKYBLOCK_KUUDRA, 10, 8, 2),
                server("open", ServerType.SKYBLOCK_KUUDRA, 100, 4, 1));
        assertEquals(List.of("open"), names(KuudraServerSelector.rank(servers, 4, _ -> 0)));
        assertEquals(List.of("full-players", "open"), names(KuudraServerSelector.rank(servers, 2, _ -> 0)));
    }

    @Test
    void prefersTheServerWithTheMostFreeRunsThenTheMostRoom() {
        List<OrchestratorCache.GameServerState> servers = List.of(
                server("one-run", ServerType.SKYBLOCK_KUUDRA, 100, 0, 1),
                server("three-runs-busy", ServerType.SKYBLOCK_KUUDRA, 100, 50, 3),
                server("three-runs-quiet", ServerType.SKYBLOCK_KUUDRA, 100, 2, 3));
        assertEquals(List.of("three-runs-quiet", "three-runs-busy", "one-run"),
                names(KuudraServerSelector.rank(servers, 1, _ -> 0)));
    }

    @Test
    void playersAlreadyOnTheirWayCountAgainstTheRoom() {
        OrchestratorCache.GameServerState busy = server("busy", ServerType.SKYBLOCK_KUUDRA, 10, 4, 4);
        OrchestratorCache.GameServerState quiet = server("quiet", ServerType.SKYBLOCK_KUUDRA, 10, 4, 4);
        Map<UUID, Integer> reserved = Map.of(busy.uuid(), 4);
        assertEquals(List.of("quiet"), names(KuudraServerSelector.rank(List.of(busy, quiet), 3,
                uuid -> reserved.getOrDefault(uuid, 0))));
    }

    @Test
    void nothingQualifiesWithoutKuudraServers() {
        assertTrue(KuudraServerSelector.rank(List.of(server("hub", ServerType.SKYBLOCK_HUB, 100, 0, null)), 1, _ -> 0).isEmpty());
    }
}
