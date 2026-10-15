package dev.daweapon.proxyapi;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTransferDataCacheTest {
    private static final String NASTY = "{\"name\":\"line\nbreak\\u0000 é ☃\"}";

    @Test
    void encodedDocumentRoundTripsWithControlAndUnicodeCharacters() {
        UUID id = UUID.randomUUID();
        PlayerTransferDataCache.put(id, PlayerTransferDataCache.encodeDocument(NASTY), null);

        assertEquals(NASTY, PlayerTransferDataCache.takeAccountDocument(id));
    }

    @Test
    void encodedFormIsPlainBase64WithoutControlCharacters() {
        String encoded = PlayerTransferDataCache.encodeDocument(NASTY);

        assertTrue(encoded.matches("[A-Za-z0-9+/=]+"));
    }

    @Test
    void takingUnknownPlayerReturnsNull() {
        assertNull(PlayerTransferDataCache.takeAccountDocument(UUID.randomUUID()));
        assertNull(PlayerTransferDataCache.takeProfileDocument(UUID.randomUUID()));
    }

    @Test
    void accountOnlyEntryIsConsumedByFirstAccountTake() {
        UUID id = UUID.randomUUID();
        PlayerTransferDataCache.put(id, PlayerTransferDataCache.encodeDocument("acct"), null);

        assertEquals("acct", PlayerTransferDataCache.takeAccountDocument(id));
        assertNull(PlayerTransferDataCache.takeAccountDocument(id));
    }

    @Test
    void accountDocumentCanBeReadAgainWhileProfileIsPending() {
        UUID id = UUID.randomUUID();
        PlayerTransferDataCache.put(id, PlayerTransferDataCache.encodeDocument("acct"),
                PlayerTransferDataCache.encodeDocument("prof"));

        assertEquals("acct", PlayerTransferDataCache.takeAccountDocument(id));
        assertEquals("acct", PlayerTransferDataCache.takeAccountDocument(id));
    }

    @Test
    void takingProfileConsumesTheWholeEntry() {
        UUID id = UUID.randomUUID();
        PlayerTransferDataCache.put(id, PlayerTransferDataCache.encodeDocument("acct"),
                PlayerTransferDataCache.encodeDocument("prof"));

        assertEquals("prof", PlayerTransferDataCache.takeProfileDocument(id));
        assertNull(PlayerTransferDataCache.takeProfileDocument(id));
        assertNull(PlayerTransferDataCache.takeAccountDocument(id));
    }

    @Test
    void entriesAreIsolatedPerPlayer() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        PlayerTransferDataCache.put(a, PlayerTransferDataCache.encodeDocument("A"), null);
        PlayerTransferDataCache.put(b, PlayerTransferDataCache.encodeDocument("B"), null);

        assertEquals("B", PlayerTransferDataCache.takeAccountDocument(b));
        assertEquals("A", PlayerTransferDataCache.takeAccountDocument(a));
    }
}
