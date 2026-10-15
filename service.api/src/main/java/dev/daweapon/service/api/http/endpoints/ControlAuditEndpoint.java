package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.AuditDatabase;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Reads the append-only action log.
 *
 * Readable by any paired device rather than owners only: the people who can press the buttons
 * are the people who should be able to see what has been pressed, and a log only its
 * administrator can read is weaker accountability, not stronger.
 */
public class ControlAuditEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/audit";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }

        int limit = parseInt(req.queryParams("limit"), 100);
        String action = req.queryParams("action");
        String actor = req.queryParams("actor");

        JsonArray entries = new JsonArray();
        for (AuditEntry entry : AuditDatabase.recent(limit, action, actor)) {
            JsonObject item = new JsonObject();
            item.addProperty("id", entry.id());
            item.addProperty("at", entry.at().toString());
            item.addProperty("actorName", entry.actorName());
            item.addProperty("deviceId", entry.deviceId());
            item.addProperty("deviceName", entry.deviceName());
            item.addProperty("role", entry.role());
            item.addProperty("action", entry.action());
            item.addProperty("outcome", entry.outcome().name());
            item.addProperty("detail", entry.detail());

            JsonArray targets = new JsonArray();
            entry.targets().forEach(targets::add);
            item.add("targets", targets);
            entries.add(item);
        }

        JsonObject payload = new JsonObject();
        payload.add("entries", entries);
        payload.addProperty("total", AuditDatabase.count());
        return APIResponse.success(payload.toString());
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
