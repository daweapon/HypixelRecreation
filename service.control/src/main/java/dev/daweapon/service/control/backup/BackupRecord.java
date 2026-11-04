package dev.daweapon.service.control.backup;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** One backup as recorded in its manifest.json. */
public record BackupRecord(
        String id,
        Instant createdAt,
        Instant finishedAt,
        Trigger trigger,
        String requestedBy,
        Status status,
        List<Part> parts
) {
    public enum Trigger {
        SCHEDULED,
        MANUAL,
        /** Taken automatically before a live restore, so a restore can itself be undone. */
        PRE_RESTORE
    }

    public enum Status {
        RUNNING,
        COMPLETE,
        PARTIAL,
        FAILED
    }

    public static final String MONGO = "mongo";
    public static final String REDIS = "redis";

    public record Part(String kind, String container, String file, long bytes, String sha256,
                       boolean ok, String message) {
        public static Part failed(String kind, String container, String message) {
            return new Part(kind, container, null, 0, null, false, message);
        }
    }

    private static final DateTimeFormatter ID_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    public static String newId(Instant at, Trigger trigger) {
        return ID_FORMAT.format(at) + "-" + trigger.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** True only for ids this agent could have produced, so a request can never name a path. */
    public static boolean isValidId(String id) {
        return id != null && id.matches("\\d{8}-\\d{6}-[a-z-]{1,20}");
    }

    public static Status statusOf(List<Part> parts) {
        long ok = parts.stream().filter(Part::ok).count();
        if (parts.isEmpty() || ok == 0) {
            return Status.FAILED;
        }
        return ok == parts.size() ? Status.COMPLETE : Status.PARTIAL;
    }

    public static BackupRecord started(String id, Instant at, Trigger trigger, String requestedBy) {
        return new BackupRecord(id, at, null, trigger, requestedBy, Status.RUNNING, List.of());
    }

    public BackupRecord finished(Instant at, List<Part> finalParts) {
        return new BackupRecord(id, createdAt, at, trigger, requestedBy, statusOf(finalParts),
                List.copyOf(finalParts));
    }

    public Part part(String kind) {
        return parts == null ? null
                : parts.stream().filter(p -> p.kind().equals(kind)).findFirst().orElse(null);
    }

    public long totalBytes() {
        return parts == null ? 0 : parts.stream().mapToLong(Part::bytes).sum();
    }
}
