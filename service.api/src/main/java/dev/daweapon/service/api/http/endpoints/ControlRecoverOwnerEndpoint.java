package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.daweapon.service.api.APIAdminDatabase;
import dev.daweapon.service.api.APIAdminDatabaseObject;
import dev.daweapon.service.api.control.AuditDatabase;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.ControlTokens;
import dev.daweapon.service.api.control.DeviceDatabase;
import dev.daweapon.service.api.control.DeviceRecord;
import dev.daweapon.service.api.control.OwnerRecovery;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Restores owner access when no owner device can be used to grant it.
 *
 * This is the break-glass path, so it carries no bearer token - the caller by definition has
 * no working credential. Authority instead comes from the same STAFF-gated in-game handshake
 * that enrolment uses, and the claim is refused outright while somebody else's owner device is
 * still checking in, because promotion through the app is the correct route whenever it exists.
 */
public class ControlRecoverOwnerEndpoint extends SkyBlockEndpoint {
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
        return "/control/recover-owner";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String sessionId = ControlEnrolEndpoint.optString(body, "sessionId", headers.get("X-Session-ID"));
        String deviceName = ControlEnrolEndpoint.optString(body, "deviceName", "recovered device");
        boolean revokePrevious = !"false".equalsIgnoreCase(
                ControlEnrolEndpoint.optString(body, "revokePrevious", "true"));

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

        UUID claimant = session.getAuthenticatorUUID();
        OwnerRecovery.Decision decision = OwnerRecovery.assess(claimant, DeviceDatabase.all(),
                Instant.now(), OwnerRecovery.configuredIdleWindow());

        if (!decision.allowed()) {
            AuditDatabase.record(AuditEntry.of(
                    session.getAuthenticatorName(), "unknown", deviceName, "none",
                    "owner-recovery", List.of(), AuditEntry.Outcome.DENIED, decision.reason()));
            return APIResponse.error("Owner recovery refused - " + decision.reason());
        }

        List<String> superseded = new ArrayList<>();
        if (revokePrevious) {
            for (DeviceRecord previous : decision.supersededDevices()) {
                DeviceDatabase.save(previous.revokedCopy());
                ControlTokens.revokeDevice(previous.deviceId());
                superseded.add(previous.deviceName());
            }
        }

        String token = ControlTokens.generateToken();
        DeviceRecord record = new DeviceRecord(
                ControlTokens.hash(token),
                UUID.randomUUID().toString(),
                deviceName,
                session.getAuthenticatorName(),
                claimant,
                ControlRole.OWNER,
                Instant.now(),
                Instant.now(),
                false
        );
        DeviceDatabase.save(record);

        AuditDatabase.record(AuditEntry.of(
                record.playerName(), record.deviceId(), deviceName, ControlRole.OWNER.name(),
                "owner-recovery", superseded, AuditEntry.Outcome.ACCEPTED,
                decision.verdict() + " - " + decision.reason()
                        + (superseded.isEmpty() ? "" : "; revoked " + String.join(", ", superseded))));

        JsonObject payload = new JsonObject();
        payload.addProperty("deviceToken", token);
        payload.addProperty("deviceId", record.deviceId());
        payload.addProperty("role", ControlRole.OWNER.name());
        payload.addProperty("playerName", record.playerName());
        payload.addProperty("verdict", decision.verdict().name());
        payload.addProperty("reason", decision.reason());
        JsonArray revoked = new JsonArray();
        superseded.forEach(revoked::add);
        payload.add("revokedDevices", revoked);
        return APIResponse.success(payload.toString());
    }
}
