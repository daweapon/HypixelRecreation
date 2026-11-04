package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.EventControl;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.HttpMethod;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.List;

/** Fires dark-auction, force-mayor, special-mayor, calendar-skip or mining-event. OWNER only, audited. */
public class ControlAdminEventTriggerEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/events/trigger";
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.OWNER;
    }

    @Override
    protected String auditAction(JsonObject body) {
        return "event-" + ControlEnrolEndpoint.optString(body, "kind", "unknown");
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception {
        String kind = ControlEnrolEndpoint.optString(body, "kind", "");
        String value = ControlEnrolEndpoint.optString(body, "value", null);
        List<String> targets = value == null ? List.of() : List.of(value);
        Logger.warn("CONTROL EVENT TRIGGER {} {} requested by {}", kind, value, caller.actor());

        EventControl.Outcome outcome;
        try {
            outcome = EventControl.trigger(kind, value);
        } catch (Exception e) {
            audit(caller, "event-" + kind, targets, false, String.valueOf(e.getMessage()));
            throw e;
        }

        audit(caller, "event-" + kind, targets, outcome.ok(), outcome.detail());
        if (!outcome.ok()) res.status(409);
        return outcome.ok() ? ok(outcome) : APIResponse.error(outcome.detail());
    }
}
