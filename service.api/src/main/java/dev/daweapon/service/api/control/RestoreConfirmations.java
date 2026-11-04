package dev.daweapon.service.api.control;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The server-side half of a restore's two-step confirmation.
 *
 * Step one previews the restore and issues a short-lived token bound to the device and to the
 * exact backup, parts and mode previewed. Step two must present it. The token is single-use and
 * cannot be replayed for a different backup or a more destructive mode than the one confirmed.
 */
public final class RestoreConfirmations {
    public static final Duration TTL = Duration.ofMinutes(2);

    private record Pending(String deviceId, String backupId, List<String> parts, String mode, Instant expiresAt) {
    }

    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    public String issue(String deviceId, String backupId, List<String> parts, String mode, Instant now) {
        pending.values().removeIf(p -> now.isAfter(p.expiresAt()));
        String token = ControlTokens.generateToken();
        pending.put(ControlTokens.hash(token), new Pending(deviceId, backupId, List.copyOf(parts),
                mode, now.plus(TTL)));
        return token;
    }

    /** Consumes the token whether or not it matches, so a wrong guess cannot be retried. */
    public boolean consume(String token, String deviceId, String backupId, List<String> parts, String mode,
                           Instant now) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Pending entry = pending.remove(ControlTokens.hash(token));
        return entry != null
                && !now.isAfter(entry.expiresAt())
                && entry.deviceId().equals(deviceId)
                && entry.backupId().equals(backupId)
                && entry.parts().equals(List.copyOf(parts))
                && entry.mode().equals(mode);
    }
}
