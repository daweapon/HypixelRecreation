package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAgentClient;
import dev.daweapon.service.api.control.ControlAudit;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.SecondFactor;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

/**
 * Changes the backup schedule or retention. OWNER, and second-factor gated once enrolled,
 * because lowering retention deletes backups immediately.
 */
public class ControlBackupSettingsEndpoint extends SkyBlockEndpoint {
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
        return "/control/backups/settings";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        JsonObject forwarded = new JsonObject();
        if (body.has("schedule")) {
            forwarded.add("schedule", body.get("schedule"));
        }
        if (body.has("retain")) {
            forwarded.add("retain", body.get("retain"));
        }
        List<String> targets = List.of(forwarded.toString());

        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            ControlAudit.refused(req, "backup-settings", targets,
                    "backup settings require OWNER: " + auth.error().getMessage());
            res.status(403);
            return auth.error();
        }
        APIResponse refusal = SecondFactor.require(req, res, auth.caller(), "backup-settings", targets);
        if (refusal != null) {
            return refusal;
        }

        ControlAgentClient.AgentResponse response = AGENT.post("/control/backups/settings",
                forwarded.toString(), auth.caller().actor());
        ControlAudit.caller(auth.caller(), "backup-settings", targets,
                response.isSuccess() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                response.isSuccess() ? "backup settings changed"
                        : "agent returned HTTP " + response.status() + ": " + response.body());
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
