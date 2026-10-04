package net.swofty.service.api.http.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.swofty.service.api.APIAdminDatabase;
import net.swofty.service.api.APIAdminDatabaseObject;
import net.swofty.service.api.control.ControlRole;
import net.swofty.service.api.control.ControlTokens;
import net.swofty.service.api.control.DeviceDatabase;
import net.swofty.service.api.control.DeviceRecord;
import net.swofty.service.api.http.APIResponse;
import net.swofty.service.api.http.EndpointType;
import net.swofty.service.api.http.HttpMethod;
import net.swofty.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Trades a completed in-game auth handshake for a long-lived device token.
 *
 * Enrolment deliberately reuses the existing flow: the app posts a code to /api/initiateAuth,
 * the operator runs /verifyapi in game (which is already STAFF-gated), and only once that has
 * attached a player to the session can the session be exchanged here.
 *
 * The first device ever enrolled becomes OWNER so the system is usable out of the box; every
 * later device starts READ_ONLY and must be promoted by an owner. A device that can stop
 * containers is never handed out by default.
 */
public class ControlEnrolEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }

    @Override
    public String getPath() {
        return "/control/enrol";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = parseBody(req);
        String sessionId = optString(body, "sessionId", headers.get("X-Session-ID"));
        String deviceName = optString(body, "deviceName", "unnamed device");

        if (sessionId == null || sessionId.isBlank()) {
            return APIResponse.error("Missing sessionId");
        }

        APIAdminDatabaseObject session = new APIAdminDatabase().getFromSessionId(sessionId);
        if (session == null) {
            return APIResponse.error("Unknown session - call /api/initiateAuth first");
        }
        if (!session.isAuthenticated()) {
            return APIResponse.error("Session not yet verified in game - run /verifyapi "
                    + session.getAuthCode() + " on the server");
        }

        boolean firstEver = DeviceDatabase.isEmpty();
        ControlRole role = firstEver ? ControlRole.OWNER : ControlRole.READ_ONLY;

        String token = ControlTokens.generateToken();
        DeviceRecord record = new DeviceRecord(
                ControlTokens.hash(token),
                UUID.randomUUID().toString(),
                deviceName,
                session.getAuthenticatorName(),
                session.getAuthenticatorUUID(),
                role,
                Instant.now(),
                Instant.now(),
                false
        );
        DeviceDatabase.save(record);

        JsonObject payload = new JsonObject();
        payload.addProperty("deviceToken", token);
        payload.addProperty("deviceId", record.deviceId());
        payload.addProperty("role", role.name());
        payload.addProperty("playerName", record.playerName());
        payload.addProperty("firstDevice", firstEver);
        return APIResponse.success(payload.toString());
    }

    static JsonObject parseBody(Request req) {
        String body = req.body();
        if (body == null || body.isBlank()) {
            return new JsonObject();
        }
        try {
            JsonObject parsed = JsonParser.parseString(body).getAsJsonObject();
            return parsed == null ? new JsonObject() : parsed;
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    static String optString(JsonObject body, String key, String fallback) {
        if (body == null || !body.has(key) || body.get(key).isJsonNull()) {
            return fallback;
        }
        return body.get(key).getAsString();
    }
}
