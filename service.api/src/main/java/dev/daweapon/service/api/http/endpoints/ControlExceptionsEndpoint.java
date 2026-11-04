package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonParser;
import dev.daweapon.service.api.control.ControlAgentClient;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.util.Map;

/** Exceptions and ERROR lines across the fleet's recent logs, grouped by signature. Read-only. */
public class ControlExceptionsEndpoint extends SkyBlockEndpoint {
    private static final ControlAgentClient AGENT = new ControlAgentClient();

    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/exceptions";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }

        int tail = 2000;
        try {
            if (req.queryParams("tail") != null) {
                tail = Integer.parseInt(req.queryParams("tail").trim());
            }
        } catch (NumberFormatException ignored) {
            // Fall back to the default window.
        }
        String path = "/control/exceptions?tail=" + tail
                + "&noise=" + "true".equalsIgnoreCase(req.queryParams("noise"))
                + "&refresh=" + "true".equalsIgnoreCase(req.queryParams("refresh"));

        ControlAgentClient.AgentResponse response = AGENT.get(path, auth.caller().actor());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
