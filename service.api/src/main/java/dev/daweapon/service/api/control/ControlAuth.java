package dev.daweapon.service.api.control;

import dev.daweapon.service.api.http.APIResponse;
import org.jetbrains.annotations.Nullable;
import spark.Request;

import java.time.Instant;

/** Resolves and role-checks the caller of a control endpoint. */
public final class ControlAuth {

    public record Caller(DeviceRecord device, ControlTokens.AccessToken accessToken) {
        public String actor() {
            return accessToken.playerName() + " (" + accessToken.deviceId() + ")";
        }

        public ControlRole role() {
            return accessToken.role();
        }
    }

    /** Thrown-free result: either a caller, or the error response to return. */
    public record Result(@Nullable Caller caller, @Nullable APIResponse error) {
        public boolean failed() {
            return caller == null;
        }
    }

    private ControlAuth() {
    }

    /**
     * Best-effort identity for a request that failed authorisation.
     *
     * A refused attempt still needs to name whoever made it where possible. When the token was
     * valid but the role insufficient the device is known; when the token itself was bad there
     * is nothing to resolve and the entry says so rather than inventing an actor.
     */
    public static String describeCaller(Request req) {
        ControlTokens.AccessToken token = resolveToken(req);
        return token == null ? "unauthenticated" : token.playerName();
    }

    public static String describeDeviceId(Request req) {
        ControlTokens.AccessToken token = resolveToken(req);
        return token == null ? "unknown" : token.deviceId();
    }

    private static @Nullable ControlTokens.AccessToken resolveToken(Request req) {
        String header = req.headers("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return ControlTokens.validateAccessToken(header.substring("Bearer ".length()).trim());
    }

    public static Result authorise(Request req, ControlRole required) {
        String header = req.headers("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return new Result(null, APIResponse.error("Unauthorized - missing bearer token"));
        }

        ControlTokens.AccessToken accessToken =
                ControlTokens.validateAccessToken(header.substring("Bearer ".length()).trim());
        if (accessToken == null) {
            return new Result(null, APIResponse.error("Unauthorized - access token invalid or expired"));
        }

        DeviceRecord device = DeviceDatabase.findByDeviceId(accessToken.deviceId());
        if (device == null || device.revoked()) {
            ControlTokens.revokeDevice(accessToken.deviceId());
            return new Result(null, APIResponse.error("Unauthorized - device revoked"));
        }

        if (!accessToken.role().satisfies(required)) {
            return new Result(null, APIResponse.error("Forbidden - requires role " + required
                    + ", this device has " + accessToken.role()));
        }

        DeviceDatabase.save(device.withLastSeen(Instant.now()));
        return new Result(new Caller(device, accessToken), null);
    }
}
