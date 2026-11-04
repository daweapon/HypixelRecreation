package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.PlayerAdmin;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

import java.util.UUID;

/** Looks a player up by name or UUID: rank, where they are, profiles, punishments and wipe state. */
public class ControlAdminPlayerEndpoint extends ControlAdminEndpoint {
    private record Payload(PlayerAdmin.Lookup player, PlayerAdmin.Options options) {}

    @Override
    public String getPath() {
        return "/control/admin/player";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception {
        UUID uuid = PlayerAdmin.resolve(req.queryParams("query"));
        if (uuid == null) {
            res.status(404);
            return APIResponse.error("No player by that name has joined this server");
        }
        return ok(new Payload(PlayerAdmin.lookup(uuid), PlayerAdmin.options()));
    }
}
