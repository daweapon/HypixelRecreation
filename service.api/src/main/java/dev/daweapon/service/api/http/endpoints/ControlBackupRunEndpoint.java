package dev.daweapon.service.api.http.endpoints;

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

/** Takes a backup now. OPERATOR: it only adds data, it never changes any. */
public class ControlBackupRunEndpoint extends SkyBlockEndpoint {
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
        return "/control/backups/run";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OPERATOR);
        if (auth.failed()) {
            ControlAudit.refused(req, "backup-run", List.of(), String.valueOf(auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }

        ControlAgentClient.AgentResponse response = AGENT.post("/control/backups/run", "{}", auth.caller().actor());
        ControlAudit.caller(auth.caller(), "backup-run", List.of(),
                response.isSuccess() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                response.isSuccess() ? "manual backup started"
                        : "agent returned HTTP " + response.status() + ": " + response.body());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
