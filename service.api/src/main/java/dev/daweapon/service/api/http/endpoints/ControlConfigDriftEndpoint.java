package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.control.ControlForwarding;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.util.Map;

/** Config drift: each game server's configuration tree against the host's. */
public class ControlConfigDriftEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/integrity/config-drift";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        boolean refresh = "true".equalsIgnoreCase(req.queryParams("refresh"));
        return ControlForwarding.read(req, res, "/control/integrity/config-drift" + (refresh ? "?refresh=true" : ""));
    }
}
