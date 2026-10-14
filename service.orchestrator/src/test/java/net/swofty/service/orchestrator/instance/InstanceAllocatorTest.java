package net.swofty.service.orchestrator.instance;

import net.swofty.commons.ServerType;
import net.swofty.commons.protocol.objects.game.InstantiateGamePushProtocol;
import net.swofty.service.orchestrator.OrchestratorCache;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanceAllocatorTest {
    private static final InstantiateGamePushProtocol.Request REQUEST = new InstantiateGamePushProtocol.Request("BASIC", null);

    private static OrchestratorCache.GameServerState server(String name) {
        return new OrchestratorCache.GameServerState(UUID.nameUUIDFromBytes(name.getBytes()), name,
                ServerType.SKYBLOCK_KUUDRA, 100, 0, System.currentTimeMillis(), List.of(), 1);
    }

    private static InstantiateGamePushProtocol.Response ok(String id) {
        return new InstantiateGamePushProtocol.Response(true, id, null, null, null);
    }

    private static InstantiateGamePushProtocol.Response refused(String error) {
        return new InstantiateGamePushProtocol.Response(false, null, null, null, error);
    }

    @Test
    void noCandidatesMeansFullWhenServersAreOnlineAndNoServerOtherwise() {
        InstanceAllocator allocator = new InstanceAllocator((_, _) -> ok("x"), (_, _) -> { });

        assertEquals(FailureReason.FULL, allocator.allocate(List.of(), true, REQUEST, 1).failure());
        assertEquals(FailureReason.NO_SERVER, allocator.allocate(List.of(), false, REQUEST, 1).failure());
    }

    @Test
    void firstServerThatStartsTheInstanceWinsAndReservesSlots() {
        List<String> reserved = new ArrayList<>();
        InstanceAllocator allocator = new InstanceAllocator(
                (server, _) -> server.shortName().equals("a") ? refused("busy") : ok("game-b"),
                (server, slots) -> reserved.add(server.shortName() + ":" + slots));

        Allocation allocation = allocator.allocate(List.of(server("a"), server("b")), true, REQUEST, 3);

        assertTrue(allocation.succeeded());
        assertEquals("b", allocation.server().shortName());
        assertEquals("game-b", allocation.gameId());
        assertEquals(List.of("b:3"), reserved);
    }

    @Test
    void reportsTheLastFailureReasonWhenEveryServerFails() {
        InstanceAllocator allocator = new InstanceAllocator((server, _) -> {
            if (server.shortName().equals("a")) throw new TimeoutException();
            return refused("nope");
        }, (_, _) -> { });

        Allocation allocation = allocator.allocate(List.of(server("a"), server("b")), true, REQUEST, 1);

        assertEquals(FailureReason.REFUSED, allocation.failure());
        assertEquals("nope", allocation.detail());
        assertNull(allocation.server());
    }

    @Test
    void timeoutIsReportedAsTimeout() {
        InstanceAllocator allocator = new InstanceAllocator((_, _) -> { throw new TimeoutException(); }, (_, _) -> { });

        assertEquals(FailureReason.TIMEOUT, allocator.allocate(List.of(server("a")), true, REQUEST, 1).failure());
    }

    @Test
    void nullResponseCountsAsRefused() {
        InstanceAllocator allocator = new InstanceAllocator((_, _) -> null, (_, _) -> { });

        assertEquals(FailureReason.REFUSED, allocator.allocate(List.of(server("a")), true, REQUEST, 1).failure());
    }
}
