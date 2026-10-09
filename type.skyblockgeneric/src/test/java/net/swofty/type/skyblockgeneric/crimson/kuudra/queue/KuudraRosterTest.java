package net.swofty.type.skyblockgeneric.crimson.kuudra.queue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraRosterTest {

    @Test
    void roundTripsThePartyInOrder() {
        List<UUID> party = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        assertEquals(party, KuudraRoster.decode(KuudraRoster.encode(party)));
    }

    @Test
    void ignoresMapsThatAreNotARoster() {
        assertTrue(KuudraRoster.decode(null).isEmpty());
        assertTrue(KuudraRoster.decode("kuudras_hollow").isEmpty());
        assertTrue(KuudraRoster.decode("roster:").isEmpty());
    }

    @Test
    void anEmptyPartyCannotBeEncoded() {
        assertThrows(IllegalArgumentException.class, () -> KuudraRoster.encode(List.of()));
    }
}
