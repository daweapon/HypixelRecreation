package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.control.ControlForwarding;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Memory budget; {@code candidates} names containers about to be started. */
public class ControlCapacityEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/integrity/capacity";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        String candidates = req.queryParams("candidates");
        return ControlForwarding.read(req, res, "/control/integrity/capacity"
                + (candidates == null || candidates.isBlank() ? ""
                : "?candidates=" + URLEncoder.encode(candidates, StandardCharsets.UTF_8)));
    }
}
