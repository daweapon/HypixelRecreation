package dev.daweapon.service.api.control;

import org.bson.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * One enrolled device.
 *
 * The token itself is never stored - only its SHA-256 hash - so a dump of this collection does
 * not hand over working credentials.
 */
public record DeviceRecord(
        String tokenHash,
        String deviceId,
        String deviceName,
        String playerName,
        UUID playerUUID,
        ControlRole role,
        Instant createdAt,
        Instant lastSeenAt,
        boolean revoked
) {
    public Document toDocument() {
        return new Document()
                .append("_id", tokenHash)
                .append("deviceId", deviceId)
                .append("deviceName", deviceName)
                .append("playerName", playerName)
                .append("playerUUID", playerUUID == null ? null : playerUUID.toString())
                .append("role", role.name())
                .append("createdAt", createdAt.toString())
                .append("lastSeenAt", lastSeenAt == null ? null : lastSeenAt.toString())
                .append("revoked", revoked);
    }

    public static DeviceRecord fromDocument(Document document) {
        String playerUuid = document.getString("playerUUID");
        String lastSeen = document.getString("lastSeenAt");
        return new DeviceRecord(
                document.getString("_id"),
                document.getString("deviceId"),
                document.getString("deviceName"),
                document.getString("playerName"),
                playerUuid == null ? null : UUID.fromString(playerUuid),
                ControlRole.parse(document.getString("role"), ControlRole.READ_ONLY),
                Instant.parse(document.getString("createdAt")),
                lastSeen == null ? null : Instant.parse(lastSeen),
                document.getBoolean("revoked", false)
        );
    }

    public DeviceRecord withLastSeen(Instant seenAt) {
        return new DeviceRecord(tokenHash, deviceId, deviceName, playerName, playerUUID,
                role, createdAt, seenAt, revoked);
    }

    public DeviceRecord withRole(ControlRole newRole) {
        return new DeviceRecord(tokenHash, deviceId, deviceName, playerName, playerUUID,
                newRole, createdAt, lastSeenAt, revoked);
    }

    public DeviceRecord revokedCopy() {
        return new DeviceRecord(tokenHash, deviceId, deviceName, playerName, playerUUID,
                role, createdAt, lastSeenAt, true);
    }
}
