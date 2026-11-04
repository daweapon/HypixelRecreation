package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.OrphanScan;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.HttpMethod;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * Deletes one orphaned island or profile. OWNER only, audited.
 *
 * The body must repeat the id in "confirm" (the app's second step), and the item must still be an
 * orphan on a fresh scan, so a stale list can never delete something that has since been claimed.
 */
public class ControlAdminOrphanDeleteEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/orphans/delete";
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
        return "orphan-delete";
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) {
        String kind = ControlEnrolEndpoint.optString(body, "kind", "");
        String id = ControlEnrolEndpoint.optString(body, "id", "");
        if (id.isBlank() || !id.equals(ControlEnrolEndpoint.optString(body, "confirm", ""))) {
            throw new IllegalArgumentException("confirm must repeat the id being deleted");
        }

        try {
            String detail = OrphanScan.delete(kind, id);
            audit(caller, "orphan-delete", List.of(kind + ":" + id), true, detail);
            return ok(new Deleted(true, detail));
        } catch (IllegalStateException | IllegalArgumentException e) {
            audit(caller, "orphan-delete", List.of(kind + ":" + id), false, e.getMessage());
            res.status(409);
            return APIResponse.error(e.getMessage());
        }
    }

    private record Deleted(boolean ok, String detail) {}
}
