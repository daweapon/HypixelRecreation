package dev.daweapon.service.replay.session;

import dev.daweapon.commons.ServerType;
import dev.daweapon.service.replay.storage.ReplayDatabase;
import dev.daweapon.type.game.replay.ReplayMetadata;
import dev.daweapon.type.game.replay.ReplayVersion;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReplaySessionManagerTest {
    private static class RecordingDatabase extends ReplayDatabase {
        final List<Integer> chunkIndexes = new ArrayList<>();
        final List<Document> metadata = new ArrayList<>();

        @Override
        public void saveReplayDataChunk(UUID replayId, int chunkIndex, byte[] compressedData, int startTick, int endTick) {
            chunkIndexes.add(chunkIndex);
        }

        @Override
        public void saveReplayMetadata(Document document) {
            metadata.add(document);
        }
    }

    private final RecordingDatabase database = new RecordingDatabase();
    private final ReplaySessionManager manager = new ReplaySessionManager(database);

    private RecordingSession start(UUID replayId, UUID player) {
        return manager.startSession(replayId, "game", ServerType.SKYBLOCK_ISLAND, "srv", "Solo", "Map", "hash",
                10L, 1.0, 2.0, Map.of(player, "Steve"), Map.of("red", List.of(player)),
                Map.of("red", new ReplayMetadata.TeamInfo("Red", "c", 0xFF0000)));
    }

    @Test
    void startedSessionIsTrackedAndLookupable() {
        UUID id = UUID.randomUUID();
        RecordingSession session = start(id, UUID.randomUUID());

        assertSame(session, manager.getSession(id));
        assertEquals(1, manager.getActiveSessionCount());
        assertNull(manager.getSession(UUID.randomUUID()));
    }

    @Test
    void batchForUnknownSessionIsIgnored() {
        manager.receiveBatch(UUID.randomUUID(), 0, 0, 1, 1, new byte[1]);

        assertEquals(0, manager.getActiveSessionCount());
    }

    @Test
    void receiveBatchAppendsToTheSession() {
        UUID id = UUID.randomUUID();
        RecordingSession session = start(id, UUID.randomUUID());

        manager.receiveBatch(id, 0, 0, 20, 2, new byte[8]);

        assertEquals(8, session.getTotalBytesReceived());
    }

    @Test
    void endingUnknownSessionFailsWithoutTouchingStorage() throws Exception {
        var result = manager.endSession(UUID.randomUUID(), 5L, 5).get(5, TimeUnit.SECONDS);

        assertFalse(result.success());
        assertEquals(0, result.totalBytes());
        assertTrue(database.metadata.isEmpty());
    }

    @Test
    void endingSessionPersistsOrderedChunksAndMetadata() throws Exception {
        UUID id = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        RecordingSession session = start(id, player);
        session.setWinnerId("red");
        session.setWinnerType("TEAM");
        manager.receiveBatch(id, 1, 20, 40, 1, new byte[3]);
        manager.receiveBatch(id, 0, 0, 20, 1, new byte[5]);

        var result = manager.endSession(id, 99L, 40).get(5, TimeUnit.SECONDS);

        assertTrue(result.success());
        assertEquals(8, result.totalBytes());
        assertEquals(8, result.compressedBytes());
        assertEquals(List.of(0, 1), database.chunkIndexes);
        assertEquals(0, manager.getActiveSessionCount());

        Document doc = database.metadata.getFirst();
        assertEquals(id.toString(), doc.getString("replayId"));
        assertEquals("SKYBLOCK_ISLAND", doc.getString("serverType"));
        assertEquals(ReplayVersion.CURRENT_VERSION, doc.getInteger("version"));
        assertEquals(99L, doc.getLong("endTime"));
        assertEquals(40, doc.getInteger("durationTicks"));
        assertEquals(8L, doc.getLong("dataSize"));
        assertEquals(List.of(player.toString()), doc.getList("players", String.class));
        assertEquals("Steve", doc.get("playerNames", Document.class).getString(player.toString()));
        assertEquals(List.of(player.toString()), doc.get("teams", Document.class).getList("red", String.class));
        assertEquals("Red", doc.get("teamInfo", Document.class).get("red", Document.class).getString("name"));
        assertEquals(0xFF0000, doc.get("teamInfo", Document.class).get("red", Document.class).getInteger("color"));
        assertEquals("red", doc.getString("winnerId"));
        assertEquals("TEAM", doc.getString("winnerType"));
    }

    @Test
    void metadataOmitsWinnerWhenNoneWasSet() throws Exception {
        UUID id = UUID.randomUUID();
        start(id, UUID.randomUUID());

        manager.endSession(id, 1L, 1).get(5, TimeUnit.SECONDS);

        Document doc = database.metadata.getFirst();
        assertFalse(doc.containsKey("winnerId"));
        assertFalse(doc.containsKey("winnerType"));
    }

    @Test
    void endedSessionCannotBeEndedTwice() throws Exception {
        UUID id = UUID.randomUUID();
        start(id, UUID.randomUUID());
        manager.endSession(id, 1L, 1).get(5, TimeUnit.SECONDS);

        var second = manager.endSession(id, 2L, 2).get(5, TimeUnit.SECONDS);

        assertFalse(second.success());
        assertEquals(1, database.metadata.size());
    }
}
