package dev.daweapon.service.api.http.endpoints;

import com.google.gson.Gson;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlIntegrityMonitor;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Proxy registration per game server, merged here because only service.api can reach the proxy
 * (over Redis) while only the agent can see containers.
 */
public class ControlRegistrationsEndpoint extends SkyBlockEndpoint {
    private static final Gson GSON = new Gson();

    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/integrity/registrations";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }
        try {
            return new APIResponse(StatusResponse.SUCCESS,
                    GSON.toJsonTree(ControlIntegrityMonitor.evaluateRegistrations(auth.caller().actor())));
        } catch (IllegalStateException e) {
            res.status(503);
            return APIResponse.error("control agent unavailable: " + e.getMessage());
        }
    }
}
