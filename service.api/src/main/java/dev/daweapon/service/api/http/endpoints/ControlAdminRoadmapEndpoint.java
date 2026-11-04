package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.RoadmapSheet;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

/** Read-only rows of the public tracker sheet (tab=roadmap|bugs|done), cached for five minutes. */
public class ControlAdminRoadmapEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/roadmap";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception {
        return ok(RoadmapSheet.fetch(RoadmapSheet.Tab.parse(req.queryParams("tab")), flag(req, "refresh")));
    }
}
