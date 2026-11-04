package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonArray;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.DrainRestart;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.util.Map;

/** Progress of a drain-and-restart, or the recent ones when no id is given. */
public class ControlDrainJobEndpoint extends SkyBlockEndpoint {

    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/drain-restart/jobs";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }

        String id = req.queryParams("id");
        if (id == null || id.isBlank()) {
            JsonArray jobs = new JsonArray();
            DrainRestart.recent().forEach(job -> jobs.add(job.toJson()));
            return new APIResponse(StatusResponse.SUCCESS, jobs);
        }
        DrainRestart.Job job = DrainRestart.find(id);
        if (job == null) {
            res.status(404);
            return APIResponse.error("No drain job " + id + " - jobs are kept in memory and lost when service.api restarts");
        }
        return new APIResponse(StatusResponse.SUCCESS, job.toJson());
    }
}
