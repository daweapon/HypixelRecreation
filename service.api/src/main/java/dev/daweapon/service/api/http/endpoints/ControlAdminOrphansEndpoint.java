package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.OrphanClassifier;
import dev.daweapon.service.api.control.admin.OrphanScan;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.TreeMap;

/** Profiles nobody owns, islands nothing points at, and co-ops pointing at missing profiles. */
public class ControlAdminOrphansEndpoint extends ControlAdminEndpoint {
    private record Payload(OrphanScan.Result scan, Map<String, Integer> counts) {}

    @Override
    public String getPath() {
        return "/control/admin/orphans";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) {
        OrphanScan.Result scan = OrphanScan.scan(flag(req, "refresh"));
        Map<String, Integer> counts = new TreeMap<>(Map.of(OrphanClassifier.PROFILE, 0, OrphanClassifier.ISLAND, 0,
                OrphanClassifier.COOP, 0));
        scan.orphans().forEach(orphan -> counts.merge(orphan.kind(), 1, Integer::sum));
        return ok(new Payload(scan, counts));
    }
}
