package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.EventControl;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

/** Each SkyBlock server's reported date against the shared clock, the mayor, Dark Auction state and trigger options. */
public class ControlAdminEventsEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/events";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception {
        return ok(EventControl.overview());
    }
}
