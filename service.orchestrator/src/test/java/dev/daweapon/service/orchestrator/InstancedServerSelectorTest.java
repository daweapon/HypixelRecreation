package dev.daweapon.service.orchestrator;

import dev.daweapon.commons.ServerType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstancedServerSelectorTest {

    private static OrchestratorCache.GameServerState server(String name, ServerType type, int maxPlayers, int online,
                                                            Integer freeRuns) {
        return new OrchestratorCache.GameServerState(UUID.nameUUIDFromBytes(name.getBytes()), name, type, maxPlayers,
                online, System.currentTimeMillis(), List.of(), freeRuns);
    }

    private static List<String> names(List<OrchestratorCache.GameServerState> servers) {
        return servers.stream().map(OrchestratorCache.GameServerState::shortName).toList();
    }

    @Test
    void onlyCatacombsServersWithAFreeRunAndRoomForThePartyQualify() {
        List<OrchestratorCache.GameServerState> servers = List.of(
                server("kuudra", ServerType.SKYBLOCK_KUUDRA, 100, 0, 5),
                server("full-runs", ServerType.SKYBLOCK_CATACOMBS, 100, 0, 0),
                server("full-players", ServerType.SKYBLOCK_CATACOMBS, 10, 8, 2),
                server("open", ServerType.SKYBLOCK_CATACOMBS, 100, 4, 1));
        assertEquals(List.of("open"), names(InstancedServerSelector.rank(ServerType.SKYBLOCK_CATACOMBS, servers, 5, _ -> 0)));
        assertEquals(List.of("full-players", "open"),
                names(InstancedServerSelector.rank(ServerType.SKYBLOCK_CATACOMBS, servers, 2, _ -> 0)));
    }

    @Test
    void prefersTheServerWithTheMostFreeRunsThenTheMostRoom() {
        List<OrchestratorCache.GameServerState> servers = List.of(
                server("one-run", ServerType.SKYBLOCK_CATACOMBS, 100, 0, 1),
                server("three-runs-busy", ServerType.SKYBLOCK_CATACOMBS, 100, 50, 3),
                server("three-runs-quiet", ServerType.SKYBLOCK_CATACOMBS, 100, 2, 3));
        assertEquals(List.of("three-runs-quiet", "three-runs-busy", "one-run"),
                names(InstancedServerSelector.rank(ServerType.SKYBLOCK_CATACOMBS, servers, 1, _ -> 0)));
    }

    @Test
    void playersAlreadyOnTheirWayCountAgainstTheRoom() {
        OrchestratorCache.GameServerState busy = server("busy", ServerType.SKYBLOCK_CATACOMBS, 10, 4, 4);
        OrchestratorCache.GameServerState quiet = server("quiet", ServerType.SKYBLOCK_CATACOMBS, 10, 4, 4);
        Map<UUID, Integer> reserved = Map.of(busy.uuid(), 4);
        assertEquals(List.of("quiet"), names(InstancedServerSelector.rank(ServerType.SKYBLOCK_CATACOMBS,
                List.of(busy, quiet), 3, uuid -> reserved.getOrDefault(uuid, 0))));
    }

    @Test
    void nothingQualifiesWithoutCatacombsServers() {
        assertTrue(InstancedServerSelector.rank(ServerType.SKYBLOCK_CATACOMBS,
                List.of(server("hub", ServerType.SKYBLOCK_HUB, 100, 0, null)), 1, _ -> 0).isEmpty());
    }
}
