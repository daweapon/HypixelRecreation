package net.swofty.service.replay.session;

import net.swofty.commons.ServerType;
import net.swofty.type.game.replay.ReplayMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecordingSessionTest {
    private RecordingSession newSession(Map<UUID, String> players) {
        return new RecordingSession(UUID.randomUUID(), "game-1", ServerType.SKYBLOCK_ISLAND, "server-1", "Solo",
                "Map", "hash", 1000L, 0.5, -0.5, players, new HashMap<>(), new HashMap<>());
    }

    @Test
    void startsEmptyWithNoHighestTick() {
        RecordingSession session = newSession(Map.of());

        assertTrue(session.getOrderedBatches().isEmpty());
        assertEquals(0, session.getHighestTick());
        assertEquals(0, session.getTotalBytesReceived());
        assertEquals(0, session.getTotalRecordableCount());
    }

    @Test
    void batchesAreReturnedInIndexOrderRegardlessOfArrivalOrder() {
        RecordingSession session = newSession(Map.of());
        session.addBatch(2, 40, 60, 1, new byte[]{1});
        session.addBatch(0, 0, 20, 1, new byte[]{2});
        session.addBatch(1, 20, 40, 1, new byte[]{3});

        List<Integer> indexes = session.getOrderedBatches().stream().map(RecordingSession.DataBatch::index).toList();

        assertEquals(List.of(0, 1, 2), indexes);
    }

    @Test
    void resendingABatchIndexReplacesTheEarlierOne() {
        RecordingSession session = newSession(Map.of());
        session.addBatch(0, 0, 20, 3, new byte[10]);
        session.addBatch(0, 0, 20, 5, new byte[4]);

        assertEquals(1, session.getOrderedBatches().size());
        assertEquals(4, session.getTotalBytesReceived());
        assertEquals(5, session.getTotalRecordableCount());
    }

    @Test
    void totalsSumAcrossBatches() {
        RecordingSession session = newSession(Map.of());
        session.addBatch(0, 0, 20, 3, new byte[10]);
        session.addBatch(1, 20, 40, 4, new byte[7]);

        assertEquals(17, session.getTotalBytesReceived());
        assertEquals(7, session.getTotalRecordableCount());
    }

    @Test
    void highestTickNeverDecreases() {
        RecordingSession session = newSession(Map.of());
        session.addBatch(1, 20, 40, 1, new byte[1]);
        session.addBatch(0, 0, 20, 1, new byte[1]);

        assertEquals(40, session.getHighestTick());
    }

    @Test
    void addingBatchRefreshesLastDataTime() throws InterruptedException {
        RecordingSession session = newSession(Map.of());
        long before = session.getLastDataTime();
        Thread.sleep(5);

        session.addBatch(0, 0, 1, 1, new byte[1]);

        assertTrue(session.getLastDataTime() > before);
    }

    @Test
    void constructorCopiesPlayerMap() {
        Map<UUID, String> players = new HashMap<>();
        UUID first = UUID.randomUUID();
        players.put(first, "First");
        RecordingSession session = newSession(players);

        players.put(UUID.randomUUID(), "Second");

        assertEquals(Map.of(first, "First"), session.getPlayers());
    }

    @Test
    void teamInfoIsKeptAsGiven() {
        Map<String, ReplayMetadata.TeamInfo> info = Map.of("red", new ReplayMetadata.TeamInfo("Red", "c", 0xFF0000));
        RecordingSession session = new RecordingSession(UUID.randomUUID(), "g", ServerType.SKYBLOCK_ISLAND, "s", "t",
                "m", "h", 0, 0, 0, Map.of(), Map.of(), info);

        assertEquals(info, session.getTeamInfo());
    }
}
