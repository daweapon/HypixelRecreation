package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.control.ControlForwarding;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.util.Map;

/** Resource-pack health; {@code verify=true} also downloads the pack and checks its SHA-1. */
public class ControlPackHealthEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/integrity/pack";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        boolean refresh = "true".equalsIgnoreCase(req.queryParams("refresh"));
        boolean verify = "true".equalsIgnoreCase(req.queryParams("verify"));
        String query = verify ? "?verify=true" : refresh ? "?refresh=true" : "";
        return ControlForwarding.read(req, res, "/control/integrity/pack" + query);
    }
}
