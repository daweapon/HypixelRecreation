package dev.daweapon.service.api.control;

import spark.Request;

import java.util.List;

/** Shorthand for the two audit shapes every control endpoint writes. */
public final class ControlAudit {
    private ControlAudit() {
    }

    public static void caller(ControlAuth.Caller caller, String action, List<String> targets,
                              AuditEntry.Outcome outcome, String detail) {
        AuditDatabase.record(AuditEntry.of(caller.device().playerName(), caller.device().deviceId(),
                caller.device().deviceName(), caller.role().name(), action, targets, outcome, detail));
    }

    public static void refused(Request req, String action, List<String> targets, String detail) {
        AuditDatabase.record(AuditEntry.of(ControlAuth.describeCaller(req), ControlAuth.describeDeviceId(req),
                null, null, action, targets, AuditEntry.Outcome.DENIED, detail));
    }
}
