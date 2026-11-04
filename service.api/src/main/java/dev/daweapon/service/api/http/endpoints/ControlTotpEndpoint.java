package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.TotpStore;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Whether the second factor is enrolled. Readable by any paired device, so the app knows to
 * prompt for a code before an OWNER action instead of discovering it from a refusal.
 */
public class ControlTotpEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/totp";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.READ_ONLY);
        if (auth.failed()) {
            res.status(401);
            return auth.error();
        }
        TotpStore.State state = TotpStore.load();
        JsonObject payload = new JsonObject();
        payload.addProperty("enrolled", state.active());
        payload.addProperty("enrolledBy", state.enrolledBy());
        payload.addProperty("enrolledAt", state.enrolledAt() == null ? null : state.enrolledAt().toString());
        payload.addProperty("pending", state.pendingSecret() != null);
        return new APIResponse(StatusResponse.SUCCESS, payload);
    }
}
