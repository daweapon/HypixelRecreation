package dev.daweapon.service.api.control;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Issues and validates the two kinds of token the app uses.
 *
 * A device token is long-lived, revocable, and only ever sent when exchanging for an access
 * token. Access tokens are short-lived and are what every control call actually carries, so a
 * token captured off the wire stops working quickly.
 *
 * Access tokens live in memory only: a restart of service.api forces clients to re-exchange,
 * which is cheap and avoids persisting short-lived credentials at all.
 */
public final class ControlTokens {
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, AccessToken> ACCESS_TOKENS = new ConcurrentHashMap<>();

    public record AccessToken(String deviceId, ControlRole role, String playerName, Instant expiresAt) {
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private ControlTokens() {
    }

    public static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static String issueAccessToken(DeviceRecord device) {
        purgeExpired();
        String token = generateToken();
        ACCESS_TOKENS.put(hash(token), new AccessToken(device.deviceId(), device.role(),
                device.playerName(), Instant.now().plus(ACCESS_TOKEN_TTL)));
        return token;
    }

    public static AccessToken validateAccessToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        AccessToken resolved = ACCESS_TOKENS.get(hash(token));
        if (resolved == null) {
            return null;
        }
        if (resolved.isExpired()) {
            ACCESS_TOKENS.remove(hash(token));
            return null;
        }
        return resolved;
    }

    public static void revokeDevice(String deviceId) {
        ACCESS_TOKENS.entrySet().removeIf(entry -> entry.getValue().deviceId().equals(deviceId));
    }

    private static void purgeExpired() {
        ACCESS_TOKENS.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
