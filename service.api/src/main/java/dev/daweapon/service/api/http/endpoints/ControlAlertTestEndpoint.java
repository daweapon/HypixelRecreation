package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAgentClient;
import dev.daweapon.service.api.control.ControlAudit;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

/** Sends a test alert through every configured transport and reports each one's result. OWNER. */
public class ControlAlertTestEndpoint extends SkyBlockEndpoint {
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
        return "/control/alerts/test";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            ControlAudit.refused(req, "alert-test", List.of(), String.valueOf(auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }

        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        JsonObject forwarded = new JsonObject();
        String message = ControlEnrolEndpoint.optString(body, "message", null);
        if (message != null && !message.isBlank()) {
            forwarded.addProperty("message", message.length() > 300 ? message.substring(0, 300) : message);
        }

        ControlAgentClient.AgentResponse response = AGENT.post("/control/alerts/test",
                forwarded.toString(), auth.caller().actor());
        ControlAudit.caller(auth.caller(), "alert-test", List.of(),
                response.isSuccess() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                response.isSuccess() ? "test alert sent: " + response.body()
                        : "agent returned HTTP " + response.status() + ": " + response.body());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
