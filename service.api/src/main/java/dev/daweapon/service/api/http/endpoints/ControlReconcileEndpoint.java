package dev.daweapon.service.api.http.endpoints;

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
 * Pushes the jar store's copy of a jar back into containers running a different one.
 *
 * OWNER, like a deploy: it replaces running code, even if only with the intended version.
 */
public class ControlReconcileEndpoint extends SkyBlockEndpoint {
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
        return "/control/integrity/reconcile";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String jar = ControlEnrolEndpoint.optString(body, "jar", "");
        List<String> targets = new ArrayList<>();
        targets.add(jar);
        if (body.has("names") && body.get("names").isJsonArray()) {
            body.getAsJsonArray("names").forEach(element -> targets.add(element.getAsString()));
        }

        if (ControlDeployEndpoint.targetsControlAgent(jar, targets)) {
            res.status(409);
            return APIResponse.error("The control agent cannot reconcile its own jar; update "
                    + "DockerFiles/jars/ServiceControl.jar and restart service_control from DSM.");
        }
        return ControlForwarding.auditedAction(req, res, ControlRole.OWNER, "reconcile", targets,
                "/control/integrity/reconcile", body);
    }
}
