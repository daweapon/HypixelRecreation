package net.swofty.service.orchestrator.instance;

import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.orchestrator.OrchestratorStatusProtocol.TypeStatus;
import net.swofty.service.orchestrator.OrchestratorCache;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapacityReportTest {
    private static OrchestratorCache.GameServerState server(String name, ServerType type, int max, int online, Integer slots) {
        return new OrchestratorCache.GameServerState(UUID.nameUUIDFromBytes(name.getBytes()), name, type, max, online,
                System.currentTimeMillis(), List.of(), slots);
    }

    @Test
    void sumsServersPlayersAndFreeSlotsPerTypeAndSortsByName() {
        List<TypeStatus> report = CapacityReport.build(List.of(
                        server("k1", ServerType.SKYBLOCK_KUUDRA, 100, 10, 3),
                        server("k2", ServerType.SKYBLOCK_KUUDRA, 100, 5, 1),
                        server("d1", ServerType.RAVENGARD_DUNGEON, 50, 0, null)),
                type -> type == ServerType.SKYBLOCK_KUUDRA ? 2 : 0,
                type -> type == ServerType.RAVENGARD_DUNGEON ? 4 : 0);

        assertEquals(2, report.size());
        TypeStatus dungeon = report.get(0);
        assertEquals(ServerType.RAVENGARD_DUNGEON, dungeon.type());
        assertEquals(0, dungeon.freeInstanceSlots());
        assertEquals(4, dungeon.queued());
        TypeStatus kuudra = report.get(1);
        assertEquals(2, kuudra.servers());
        assertEquals(4, kuudra.freeInstanceSlots());
        assertEquals(15, kuudra.onlinePlayers());
        assertEquals(200, kuudra.maxPlayers());
        assertEquals(2, kuudra.instances());
    }
}
