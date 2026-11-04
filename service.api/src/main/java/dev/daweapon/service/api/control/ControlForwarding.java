package dev.daweapon.service.api.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.StatusResponse;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * The two shapes every fleet-integrity endpoint takes: an authenticated read passed through to
 * the agent, or a role-gated action passed through and audited, refusals included.
 */
public final class ControlForwarding {
    private static final ControlAgentClient AGENT = new ControlAgentClient();

    private ControlForwarding() {
    }

    public static APIResponse read(Request req, Response res, String agentPath) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }
        return relay(res, AGENT.get(agentPath, auth.caller().actor()));
    }

    public static APIResponse auditedAction(Request req, Response res, ControlRole required, String action,
                                            List<String> targets, String agentPath, JsonObject body) {
        ControlAuth.Result auth = ControlAuth.authorise(req, required);
        if (auth.failed()) {
            AuditDatabase.record(AuditEntry.of(
                    ControlAuth.describeCaller(req), ControlAuth.describeDeviceId(req), null, null,
                    action, targets, AuditEntry.Outcome.DENIED,
                    action + " requires " + required + ": " + auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }

        Logger.warn("CONTROL {} requested by {} ({})", action, auth.caller().actor(), body);
        ControlAgentClient.AgentResponse response = AGENT.post(agentPath, body.toString(), auth.caller().actor());

        AuditDatabase.record(AuditEntry.of(
                auth.caller().device().playerName(),
                auth.caller().device().deviceId(),
                auth.caller().device().deviceName(),
                auth.caller().role().name(),
                action, targets,
                response.isSuccess() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                response.isSuccess() ? action + " dispatched to control agent"
                        : "agent returned HTTP " + response.status() + ": " + response.body()));
        return relay(res, response);
    }

    private static APIResponse relay(Response res, ControlAgentClient.AgentResponse response) {
        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
