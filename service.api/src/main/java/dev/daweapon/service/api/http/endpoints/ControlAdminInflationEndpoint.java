package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.InflationPanel;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

/** Coin sources and sinks per day, transfer channels, staff tools and the money supply. */
public class ControlAdminInflationEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/economy/inflation";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) {
        int days = 30;
        String requested = req.queryParams("days");
        if (requested != null) {
            try {
                days = Integer.parseInt(requested);
            } catch (NumberFormatException ignored) {
                return APIResponse.error("days must be a number");
            }
        }
        return ok(InflationPanel.report(days, flag(req, "refresh")));
    }
}
