package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.AuditDatabase;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAgentClient;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.DrainRestart;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

/**
 * Sends a game server's players elsewhere, waits for it to empty, then restarts it.
 *
 * Requires OPERATOR, the same grant as a plain restart. Returns at once with a job to poll;
 * the outcome is audited when the job ends.
 */
public class ControlDrainRestartEndpoint extends SkyBlockEndpoint {
    private static final ControlAgentClient AGENT = new ControlAgentClient();

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
        return "/control/drain-restart";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String name = ControlEnrolEndpoint.optString(body, "name", "");

        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OPERATOR);
        if (auth.failed()) {
            AuditDatabase.record(AuditEntry.of(
                    ControlAuth.describeCaller(req), ControlAuth.describeDeviceId(req), null, null,
                    "drain-restart", name.isBlank() ? List.of() : List.of(name),
                    AuditEntry.Outcome.DENIED, String.valueOf(auth.error().getMessage())));
            res.status(403);
            return auth.error();
        }
        if (name.isBlank()) {
            res.status(400);
            return APIResponse.error("drain-restart requires a container name");
        }

        int timeout;
        try {
            timeout = Integer.parseInt(ControlEnrolEndpoint.optString(body, "timeoutSeconds", "0"));
        } catch (NumberFormatException e) {
            timeout = 0;
        }

        Logger.warn("CONTROL DRAIN-RESTART {} requested by {}", name, auth.caller().actor());
        DrainRestart.Job job = DrainRestart.start(name, timeout, auth.caller(), AGENT);
        return new APIResponse(StatusResponse.SUCCESS, job.toJson());
    }
}
