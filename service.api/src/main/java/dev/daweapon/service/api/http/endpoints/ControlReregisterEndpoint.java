package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonArray;
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
 * Re-registers DELISTED game servers with the proxy.
 *
 * A game server registers only while booting and the proxy never re-adds one it dropped, so
 * the only way back is a restart. This is that restart under its own audited action name, so
 * the log says why it happened. OPERATOR, the same grant as any restart.
 */
public class ControlReregisterEndpoint extends SkyBlockEndpoint {
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
        return "/control/integrity/reregister";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        List<String> names = new ArrayList<>();
        if (body.has("names") && body.get("names").isJsonArray()) {
            body.getAsJsonArray("names").forEach(element -> names.add(element.getAsString()));
        }
        if (names.isEmpty() || names.stream().anyMatch(name -> !isGameServer(name))) {
            res.status(400);
            return APIResponse.error("Re-register takes a non-empty names array of game servers only");
        }

        JsonObject forwarded = new JsonObject();
        JsonArray array = new JsonArray();
        names.forEach(array::add);
        forwarded.add("names", array);
        return ControlForwarding.auditedAction(req, res, ControlRole.OPERATOR, "re-register", names,
                "/control/containers/restart", forwarded);
    }

    static boolean isGameServer(String name) {
        return name.startsWith("hypixelcore_") || name.equals("prototype_lobby");
    }
}
