package net.swofty.service.api.http.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.swofty.service.api.control.ControlAgentClient;
import net.swofty.service.api.control.ControlAuth;
import net.swofty.service.api.control.ControlRole;
import net.swofty.service.api.http.APIResponse;
import net.swofty.service.api.http.EndpointType;
import net.swofty.service.api.http.HttpMethod;
import net.swofty.service.api.http.SkyBlockEndpoint;
import net.swofty.service.api.http.StatusResponse;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.Set;

/**
 * Lifecycle control: start, stop or restart containers, or the whole fleet.
 *
 * Requires OPERATOR. Every accepted action is logged with the acting device so there is a
 * record of who stopped what.
 */
public class ControlActionEndpoint extends SkyBlockEndpoint {
    private static final ControlAgentClient AGENT = new ControlAgentClient();
    private static final Set<String> CONTAINER_ACTIONS = Set.of("start", "stop", "restart");

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
        return "/control/action";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OPERATOR);
        if (auth.failed()) {
            res.status(403);
            return auth.error();
        }

        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String action = ControlEnrolEndpoint.optString(body, "action", "");
        String actor = auth.caller().actor();

        String path;
        JsonObject forwarded = new JsonObject();
        if (body.has("stagger")) {
            forwarded.add("stagger", body.get("stagger"));
        }

        if (action.equals("fleet-start")) {
            path = "/control/fleet/start";
        } else if (action.equals("fleet-stop")) {
            path = "/control/fleet/stop";
        } else if (CONTAINER_ACTIONS.contains(action)) {
            if (!body.has("names") || !body.get("names").isJsonArray()
                    || body.getAsJsonArray("names").isEmpty()) {
                res.status(400);
                return APIResponse.error("Action '" + action + "' requires a non-empty names array");
            }
            forwarded.add("names", body.get("names"));
            path = "/control/containers/" + action;
        } else {
            res.status(400);
            return APIResponse.error("Unknown action '" + action
                    + "' - expected one of start, stop, restart, fleet-start, fleet-stop");
        }

        Logger.warn("CONTROL ACTION {} requested by {} ({})", action, actor, forwarded);

        ControlAgentClient.AgentResponse response = AGENT.post(path, forwarded.toString(), actor);
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
