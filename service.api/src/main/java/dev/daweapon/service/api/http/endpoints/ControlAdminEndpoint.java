package dev.daweapon.service.api.http.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import dev.daweapon.service.api.control.AuditDatabase;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Shared shape of the game-data admin endpoints: role check, DENIED auditing for mutations, and
 * one error mapping so a game server that never answered reads as 503 rather than a bare 500.
 */
public abstract class ControlAdminEndpoint extends SkyBlockEndpoint {
    protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (value, type, context) -> new JsonPrimitive(value.toString()))
            .create();

    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    protected abstract ControlRole requiredRole();

    protected @Nullable String auditAction(JsonObject body) {
        return null;
    }

    protected abstract APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception;

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        ControlAuth.Result auth = ControlAuth.authorise(req, requiredRole());
        if (auth.failed()) {
            String action = auditAction(body);
            if (action != null) {
                AuditDatabase.record(AuditEntry.of(ControlAuth.describeCaller(req), ControlAuth.describeDeviceId(req),
                        null, null, action, List.of(), AuditEntry.Outcome.DENIED, String.valueOf(auth.error().getMessage())));
            }
            res.status(String.valueOf(auth.error().getMessage()).startsWith("Forbidden") ? 403 : 401);
            return auth.error();
        }

        try {
            return serve(auth.caller(), body, req, res);
        } catch (IllegalArgumentException e) {
            res.status(400);
            return APIResponse.error(e.getMessage());
        } catch (TimeoutException | ExecutionException e) {
            Logger.warn(e, "{} could not reach the game layer", getPath());
            res.status(503);
            return APIResponse.error("game servers or services did not answer: " + rootMessage(e));
        } catch (Exception e) {
            Logger.error(e, "{} failed", getPath());
            res.status(503);
            return APIResponse.error(rootMessage(e));
        }
    }

    protected static APIResponse ok(Object payload) {
        return new APIResponse(StatusResponse.SUCCESS, GSON.toJsonTree(payload));
    }

    protected static void audit(ControlAuth.Caller caller, String action, List<String> targets, boolean ok, String detail) {
        AuditDatabase.record(AuditEntry.of(caller.device().playerName(), caller.device().deviceId(),
                caller.device().deviceName(), caller.role().name(), action, targets,
                ok ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED, detail));
    }

    protected static UUID staffUuid(ControlAuth.Caller caller) {
        UUID uuid = caller.device().playerUUID();
        return uuid == null ? new UUID(0L, 0L) : uuid;
    }

    protected static boolean flag(Request req, String name) {
        return "true".equalsIgnoreCase(req.queryParams(name));
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
