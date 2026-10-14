package net.swofty.service.friend;

import net.swofty.commons.presence.PresenceInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PresenceStorageTest {

    private static PresenceInfo presence(UUID id, boolean online, String type, String serverId, long lastSeen) {
        return new PresenceInfo(id, online, type, serverId, lastSeen);
    }

    @Test
    void upsertReplacesAndGetReturnsLatest() {
        UUID id = UUID.randomUUID();
        PresenceStorage.upsert(presence(id, true, "HUB", "a", 10));
        PresenceStorage.upsert(presence(id, false, null, null, 20));
        assertFalse(PresenceStorage.get(id).isOnline());
        assertEquals(20, PresenceStorage.get(id).getLastSeen());
    }

    @Test
    void upsertAndGetPreviousReturnsOldEntry() {
        UUID id = UUID.randomUUID();
        assertNull(PresenceStorage.upsertAndGetPrevious(presence(id, true, "HUB", "a", 10)));
        PresenceInfo previous = PresenceStorage.upsertAndGetPrevious(presence(id, false, null, null, 20));
        assertEquals("HUB", previous.getServerType());
    }

    @Test
    void preservingServerKeepsPreviousMetadataWhenIncomingIsNull() {
        UUID id = UUID.randomUUID();
        PresenceStorage.upsert(presence(id, true, "HUB", "srv-1", 100));

        PresenceInfo previous = PresenceStorage.upsertPreservingServer(presence(id, false, null, null, 0));

        assertEquals(100, previous.getLastSeen());
        PresenceInfo merged = PresenceStorage.get(id);
        assertFalse(merged.isOnline());
        assertEquals("HUB", merged.getServerType());
        assertEquals("srv-1", merged.getServerId());
        assertEquals(100, merged.getLastSeen());
    }

    @Test
    void preservingServerPrefersIncomingNonNullValues() {
        UUID id = UUID.randomUUID();
        PresenceStorage.upsert(presence(id, true, "HUB", "srv-1", 100));

        PresenceStorage.upsertPreservingServer(presence(id, true, "ISLAND", "srv-2", 200));

        PresenceInfo merged = PresenceStorage.get(id);
        assertEquals("ISLAND", merged.getServerType());
        assertEquals("srv-2", merged.getServerId());
        assertEquals(200, merged.getLastSeen());
    }

    @Test
    void preservingServerOnFirstEntryStoresIncomingAndReturnsNull() {
        UUID id = UUID.randomUUID();
        assertNull(PresenceStorage.upsertPreservingServer(presence(id, true, null, null, 5)));
        assertTrue(PresenceStorage.get(id).isOnline());
    }

    @Test
    void getBulkAndGetMapSkipUnknownPlayers() {
        UUID known = UUID.randomUUID();
        UUID unknown = UUID.randomUUID();
        PresenceStorage.upsert(presence(known, true, "HUB", "a", 1));

        List<PresenceInfo> bulk = PresenceStorage.getBulk(List.of(known, unknown));
        Map<UUID, PresenceInfo> map = PresenceStorage.getMap(Set.of(known, unknown));

        assertEquals(1, bulk.size());
        assertEquals(Set.of(known), map.keySet());
    }

    @Test
    void emptyOrNullInputsReturnEmptyCollections() {
        assertTrue(PresenceStorage.getBulk(null).isEmpty());
        assertTrue(PresenceStorage.getBulk(List.of()).isEmpty());
        assertTrue(PresenceStorage.getMap(null).isEmpty());
        assertTrue(PresenceStorage.getOnlineStatus(List.of()).isEmpty());
    }

    @Test
    void onlineStatusDefaultsToOfflineForUnknownPlayers() {
        UUID online = UUID.randomUUID();
        UUID offline = UUID.randomUUID();
        UUID unknown = UUID.randomUUID();
        PresenceStorage.upsert(presence(online, true, "HUB", "a", 1));
        PresenceStorage.upsert(presence(offline, false, null, null, 1));

        Map<UUID, Boolean> status = PresenceStorage.getOnlineStatus(List.of(online, offline, unknown));

        assertTrue(status.get(online));
        assertFalse(status.get(offline));
        assertFalse(status.get(unknown));
    }

    @Test
    void onlineStatusToleratesDuplicateUuids() {
        UUID id = UUID.randomUUID();
        PresenceStorage.upsert(presence(id, true, "HUB", "a", 1));
        assertEquals(Map.of(id, true), PresenceStorage.getOnlineStatus(List.of(id, id)));
    }
}
