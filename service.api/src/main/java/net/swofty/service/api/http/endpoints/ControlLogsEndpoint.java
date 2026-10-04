package net.swofty.service.api.http.endpoints;

import com.google.gson.JsonParser;
import net.swofty.service.api.control.ControlAgentClient;
import net.swofty.service.api.control.ControlAuth;
import net.swofty.service.api.control.ControlRole;
import net.swofty.service.api.http.APIResponse;
import net.swofty.service.api.http.EndpointType;
import net.swofty.service.api.http.SkyBlockEndpoint;
import net.swofty.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Tail of a container's logs, boot-race noise filtered unless mode=raw. */
public class ControlLogsEndpoint extends SkyBlockEndpoint {
    private static final ControlAgentClient AGENT = new ControlAgentClient();

    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/logs";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }

        String container = req.queryParams("container");
        if (container == null || container.isBlank()) {
            res.status(400);
            return APIResponse.error("Missing container parameter");
        }

        String tail = req.queryParams("tail") == null ? "200" : req.queryParams("tail");
        String mode = req.queryParams("mode") == null ? "filtered" : req.queryParams("mode");

        String path = "/control/containers/" + URLEncoder.encode(container, StandardCharsets.UTF_8)
                + "/logs?tail=" + URLEncoder.encode(tail, StandardCharsets.UTF_8)
                + "&mode=" + URLEncoder.encode(mode, StandardCharsets.UTF_8);

        ControlAgentClient.AgentResponse response = AGENT.get(path, auth.caller().actor());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
