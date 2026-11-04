package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAgentClient;
import dev.daweapon.service.api.control.ControlAudit;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.RestoreConfirmations;
import dev.daweapon.service.api.control.SecondFactor;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Restores a backup. OWNER only, two-step, audited at both steps.
 *
 * Step one (no {@code confirmToken}) asks the agent to preview the restore and returns that
 * preview with a two-minute, single-use token bound to this device, backup, parts and mode.
 * Step two repeats the request with the token (and the TOTP code once enrolled) and runs it.
 * Nothing is touched by step one, and step two cannot be reached without having seen it.
 */
public class ControlBackupRestoreEndpoint extends SkyBlockEndpoint {
    private static final ControlAgentClient AGENT = new ControlAgentClient();
    private static final RestoreConfirmations CONFIRMATIONS = new RestoreConfirmations();

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
        return "/control/backups/restore";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String id = ControlEnrolEndpoint.optString(body, "id", "");
        String mode = ControlEnrolEndpoint.optString(body, "mode", "scratch").toLowerCase(Locale.ROOT);
        List<String> parts = readParts(body);
        String confirmToken = ControlEnrolEndpoint.optString(body, "confirmToken", null);
        List<String> targets = new ArrayList<>();
        targets.add(id);
        targets.addAll(parts);
        targets.add(mode);

        String action = confirmToken == null ? "restore-request" : "restore";
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            ControlAudit.refused(req, action, targets, "restore requires OWNER: " + auth.error().getMessage());
            res.status(403);
            return auth.error();
        }
        if (id.isBlank() || parts.isEmpty()) {
            res.status(400);
            return APIResponse.error("Missing backup id or parts (mongo, redis)");
        }
        if (!mode.equals("scratch") && !mode.equals("live")) {
            res.status(400);
            return APIResponse.error("Mode must be scratch or live");
        }

        JsonObject forwarded = new JsonObject();
        forwarded.addProperty("id", id);
        JsonArray partsArray = new JsonArray();
        parts.forEach(partsArray::add);
        forwarded.add("parts", partsArray);
        forwarded.addProperty("mode", mode);
        String deviceId = auth.caller().device().deviceId();

        if (confirmToken == null) {
            ControlAgentClient.AgentResponse preview = AGENT.post("/control/backups/restore/preview",
                    forwarded.toString(), auth.caller().actor());
            if (!preview.isSuccess()) {
                ControlAudit.caller(auth.caller(), action, targets, AuditEntry.Outcome.FAILED,
                        "preview refused: HTTP " + preview.status() + ": " + preview.body());
                res.status(preview.status());
                return APIResponse.error(preview.body());
            }
            String token = CONFIRMATIONS.issue(deviceId, id, parts, mode, Instant.now());
            ControlAudit.caller(auth.caller(), action, targets, AuditEntry.Outcome.ACCEPTED,
                    "restore previewed; awaiting confirmation");

            JsonObject payload = new JsonObject();
            payload.addProperty("confirmToken", token);
            payload.addProperty("expiresInSeconds", RestoreConfirmations.TTL.toSeconds());
            payload.addProperty("secondFactorRequired", secondFactorEnrolled());
            payload.add("preview", JsonParser.parseString(preview.body()));
            return new APIResponse(StatusResponse.SUCCESS, payload);
        }

        if (!CONFIRMATIONS.consume(confirmToken, deviceId, id, parts, mode, Instant.now())) {
            ControlAudit.caller(auth.caller(), action, targets, AuditEntry.Outcome.DENIED,
                    "confirmation token missing, expired, reused or for a different restore");
            res.status(409);
            return APIResponse.error("Confirmation expired or does not match this restore - preview it again");
        }
        APIResponse refusal = SecondFactor.require(req, res, auth.caller(), action, targets);
        if (refusal != null) {
            return refusal;
        }

        Logger.warn("RESTORE {} ({} {}) confirmed by {}", id, parts, mode, auth.caller().actor());
        ControlAgentClient.AgentResponse response = AGENT.post("/control/backups/restore",
                forwarded.toString(), auth.caller().actor());
        ControlAudit.caller(auth.caller(), action, targets,
                response.isSuccess() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                response.isSuccess() ? "restore dispatched to control agent"
                        : "agent returned HTTP " + response.status() + ": " + response.body());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }

    private static boolean secondFactorEnrolled() {
        try {
            return SecondFactor.isEnrolled();
        } catch (Exception e) {
            return true;
        }
    }

    static List<String> readParts(JsonObject body) {
        if (!body.has("parts") || !body.get("parts").isJsonArray()) {
            return List.of();
        }
        return body.getAsJsonArray("parts").asList().stream()
                .filter(element -> !element.isJsonNull())
                .map(JsonElement::getAsString)
                .map(part -> part.trim().toLowerCase(Locale.ROOT))
                .filter(part -> !part.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
}
