package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.EconomyPanel;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

/** Read-only economy health: pending Bazaar transactions, order and auction counts, purse total, Dark Auction. */
public class ControlAdminEconomyEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/economy";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) {
        return ok(EconomyPanel.snapshot(flag(req, "refresh")));
    }
}
