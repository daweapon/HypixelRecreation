package net.swofty.service.friend;

import net.swofty.commons.presence.PresenceInfo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FriendCacheFormattingTest {

    private static PresenceInfo info(String type, String id) {
        return new PresenceInfo(UUID.randomUUID(), true, type, id, 0);
    }

    @Test
    void storedNameIsTrimmedAndUnquoted() {
        assertEquals("Steve", FriendCache.parseStoredName("  \"Steve\" "));
        assertEquals("Alex", FriendCache.parseStoredName("Alex"));
    }

    @Test
    void blankOrNullStoredNameIsNull() {
        assertNull(FriendCache.parseStoredName(null));
        assertNull(FriendCache.parseStoredName("   "));
        assertNull(FriendCache.parseStoredName("\"\""));
    }

    @Test
    void lonelyQuoteIsKept() {
        assertEquals("\"", FriendCache.parseStoredName("\""));
    }

    @Test
    void serverDisplayHidesRawUuidIds() {
        assertEquals("HUB", FriendCache.formatServerDisplay(info("HUB", UUID.randomUUID().toString())));
    }

    @Test
    void serverDisplayCombinesTypeAndReadableId() {
        assertEquals("HUB - hub-3", FriendCache.formatServerDisplay(info("HUB", "hub-3")));
    }

    @Test
    void serverDisplayFallsBackToWhicheverIsPresent() {
        assertEquals("HUB", FriendCache.formatServerDisplay(info("HUB", null)));
        assertEquals("hub-3", FriendCache.formatServerDisplay(info(null, "hub-3")));
        assertNull(FriendCache.formatServerDisplay(info(null, null)));
    }
}
