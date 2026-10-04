package net.swofty.service.api;

import net.swofty.service.api.control.AuditEntry;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditEntryTest {

    @Test
    void roundTripsThroughMongoDocumentWithoutLoss() {
        AuditEntry original = AuditEntry.of("daweapon", "device-1", "pixel", "OPERATOR",
                "restart", List.of("service_party", "hypixel_proxy"),
                AuditEntry.Outcome.ACCEPTED, "dispatched to control agent");

        AuditEntry restored = AuditEntry.fromDocument(original.toDocument());

        assertEquals(original.id(), restored.id());
        assertEquals(original.actorName(), restored.actorName());
        assertEquals(original.deviceId(), restored.deviceId());
        assertEquals(original.role(), restored.role());
        assertEquals(original.action(), restored.action());
        assertEquals(original.targets(), restored.targets());
        assertEquals(original.outcome(), restored.outcome());
        assertEquals(original.detail(), restored.detail());
        assertEquals(original.at(), restored.at());
    }

    @Test
    void deniedAttemptsAreRepresentable() {
        AuditEntry denied = AuditEntry.of("unauthenticated", "unknown", null, null,
                "fleet-stop", List.of(), AuditEntry.Outcome.DENIED, "requires role OPERATOR");
        assertEquals(AuditEntry.Outcome.DENIED, denied.outcome());
        assertEquals(AuditEntry.Outcome.DENIED,
                AuditEntry.fromDocument(denied.toDocument()).outcome());
    }

    @Test
    void entriesGetDistinctIdentities() {
        AuditEntry a = AuditEntry.of("x", "d", "n", "OWNER", "start", List.of(),
                AuditEntry.Outcome.ACCEPTED, null);
        AuditEntry b = AuditEntry.of("x", "d", "n", "OWNER", "start", List.of(),
                AuditEntry.Outcome.ACCEPTED, null);
        assertNotEquals(a.id(), b.id(), "each recorded action must be its own row");
    }

    @Test
    void nullTargetsBecomeAnEmptyListRatherThanNull() {
        AuditEntry entry = AuditEntry.of("x", "d", "n", "OWNER", "fleet-start", null,
                AuditEntry.Outcome.ACCEPTED, null);
        assertTrue(entry.targets().isEmpty());
        assertTrue(AuditEntry.fromDocument(entry.toDocument()).targets().isEmpty());
    }

    @Test
    void anUnrecognisedStoredOutcomeDegradesRatherThanThrowing() {
        Document document = AuditEntry.of("x", "d", "n", "OWNER", "start", List.of(),
                AuditEntry.Outcome.ACCEPTED, null).toDocument();
        document.put("outcome", "SOMETHING_NEW");
        assertEquals(AuditEntry.Outcome.FAILED, AuditEntry.fromDocument(document).outcome(),
                "a future outcome value must not make old entries unreadable");
    }
}
