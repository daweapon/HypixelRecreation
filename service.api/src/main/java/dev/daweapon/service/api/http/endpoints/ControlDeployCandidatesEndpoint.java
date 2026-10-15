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

/** Lists jars staged in the drop folder, with their hashes and resolved target containers. */
public class ControlDeployCandidatesEndpoint extends SkyBlockEndpoint {
    private static final ControlAgentClient AGENT = new ControlAgentClient();

    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/deploy/candidates";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        // Listing is harmless and useful to an operator deciding whether a deploy is needed;
        // only performing one requires OWNER.
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OPERATOR);
        if (auth.failed()) {
            res.status(403);
            return auth.error();
        }

        ControlAgentClient.AgentResponse response =
                AGENT.get("/control/deploy/candidates", auth.caller().actor());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
