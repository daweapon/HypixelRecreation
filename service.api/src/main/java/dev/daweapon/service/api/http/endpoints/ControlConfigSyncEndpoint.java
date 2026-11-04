package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlForwarding;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copies drifted host configuration files into containers, then restarts them by default.
 *
 * OWNER: it changes what running servers load, which is a content change like a deploy.
 */
public class ControlConfigSyncEndpoint extends SkyBlockEndpoint {
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
        return "/control/integrity/config-sync";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        List<String> targets = new ArrayList<>();
        if (body.has("names") && body.get("names").isJsonArray()) {
            body.getAsJsonArray("names").forEach(element -> targets.add(element.getAsString()));
        }
        if (targets.isEmpty()) {
            targets.add("all drifted game servers");
        }
        return ControlForwarding.auditedAction(req, res, ControlRole.OWNER, "config-sync", targets,
                "/control/integrity/config-sync", body);
    }
}
