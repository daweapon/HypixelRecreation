package net.swofty.service.api.control;

import org.bson.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One recorded action.
 *
 * Denied attempts are recorded alongside successful ones: "who tried to stop the fleet and was
 * refused" is at least as interesting as who succeeded, and a log that only contains successes
 * cannot answer that.
 */
public record AuditEntry(
        String id,
        Instant at,
        String actorName,
        String deviceId,
        String deviceName,
        String role,
        String action,
        List<String> targets,
        Outcome outcome,
        String detail
) {
    public enum Outcome {
        ACCEPTED,
        DENIED,
        FAILED
    }

    public static AuditEntry of(String actorName, String deviceId, String deviceName, String role,
                                String action, List<String> targets, Outcome outcome, String detail) {
        return new AuditEntry(UUID.randomUUID().toString(), Instant.now(), actorName, deviceId,
                deviceName, role, action, targets == null ? List.of() : targets, outcome, detail);
    }

    public Document toDocument() {
        return new Document()
                .append("_id", id)
                .append("at", at.toString())
                .append("actorName", actorName)
                .append("deviceId", deviceId)
                .append("deviceName", deviceName)
                .append("role", role)
                .append("action", action)
                .append("targets", targets)
                .append("outcome", outcome.name())
                .append("detail", detail);
    }

    @SuppressWarnings("unchecked")
    public static AuditEntry fromDocument(Document document) {
        List<String> targets = (List<String>) document.get("targets");
        return new AuditEntry(
                document.getString("_id"),
                Instant.parse(document.getString("at")),
                document.getString("actorName"),
                document.getString("deviceId"),
                document.getString("deviceName"),
                document.getString("role"),
                document.getString("action"),
                targets == null ? List.of() : targets,
                parseOutcome(document.getString("outcome")),
                document.getString("detail")
        );
    }

    private static Outcome parseOutcome(String value) {
        if (value == null) {
            return Outcome.FAILED;
        }
        try {
            return Outcome.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Outcome.FAILED;
        }
    }
}
