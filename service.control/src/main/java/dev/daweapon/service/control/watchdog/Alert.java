package dev.daweapon.service.control.watchdog;

import java.time.Instant;

/** One thing the operator needs to know about, right now. */
public record Alert(
        Instant at,
        Severity severity,
        String key,
        String title,
        String body
) {
    public enum Severity {
        CRITICAL,
        WARNING,
        RECOVERY
    }

    public static Alert critical(String key, String title, String body) {
        return new Alert(Instant.now(), Severity.CRITICAL, key, title, body);
    }

    public static Alert warning(String key, String title, String body) {
        return new Alert(Instant.now(), Severity.WARNING, key, title, body);
    }

    public static Alert recovery(String key, String title, String body) {
        return new Alert(Instant.now(), Severity.RECOVERY, key, title, body);
    }
}
