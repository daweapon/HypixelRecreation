package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.control.ControlForwarding;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.util.Map;

/** Version skew: the sha256 of the jar inside each container against the jar store. */
public class ControlSkewEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/integrity/skew";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        boolean refresh = "true".equalsIgnoreCase(req.queryParams("refresh"));
        return ControlForwarding.read(req, res, "/control/integrity/skew" + (refresh ? "?refresh=true" : ""));
    }
}
