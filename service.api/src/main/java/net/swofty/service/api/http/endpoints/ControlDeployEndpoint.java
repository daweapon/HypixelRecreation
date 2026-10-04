package net.swofty.service.api.http.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.swofty.service.api.control.AuditDatabase;
import net.swofty.service.api.control.AuditEntry;
import net.swofty.service.api.control.ControlAgentClient;
import net.swofty.service.api.control.ControlAuth;
import net.swofty.service.api.control.ControlRole;
import net.swofty.service.api.http.APIResponse;
import net.swofty.service.api.http.EndpointType;
import net.swofty.service.api.http.HttpMethod;
import net.swofty.service.api.http.SkyBlockEndpoint;
import net.swofty.service.api.http.StatusResponse;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deploys a staged jar.
 *
 * Gated at OWNER rather than OPERATOR deliberately: start/stop restarts existing code, whereas
 * a deploy pushes arbitrary new code onto the server. Those are different grants and should
 * not share a role.
 */
public class ControlDeployEndpoint extends SkyBlockEndpoint {
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
        return "/control/deploy";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String jar = ControlEnrolEndpoint.optString(body, "jar", "");

        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            AuditDatabase.record(AuditEntry.of(
                    ControlAuth.describeCaller(req), ControlAuth.describeDeviceId(req), null, null,
                    "deploy", List.of(jar), AuditEntry.Outcome.DENIED,
                    "deploy requires OWNER: " + auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }

        if (jar.isBlank()) {
            res.status(400);
            return APIResponse.error("Missing jar name");
        }

        String actor = auth.caller().actor();
        Logger.warn("DEPLOY {} requested by {}", jar, actor);

        ControlAgentClient.AgentResponse response = AGENT.post("/control/deploy", body.toString(), actor);

        List<String> targets = new ArrayList<>();
        targets.add(jar);
        if (body.has("names") && body.get("names").isJsonArray()) {
            JsonArray names = body.getAsJsonArray("names");
            names.forEach(element -> targets.add(element.getAsString()));
        }

        AuditDatabase.record(AuditEntry.of(
                auth.caller().device().playerName(),
                auth.caller().device().deviceId(),
                auth.caller().device().deviceName(),
                auth.caller().role().name(),
                "deploy", targets,
                response.isSuccess() ? AuditEntry.Outcome.ACCEPTED : AuditEntry.Outcome.FAILED,
                response.isSuccess() ? "deploy dispatched to control agent"
                        : "agent returned HTTP " + response.status() + ": " + response.body()));

        if (!response.isSuccess()) {
            res.status(response.status());
            return APIResponse.error(response.body());
        }
        return new APIResponse(StatusResponse.SUCCESS, JsonParser.parseString(response.body()));
    }
}
