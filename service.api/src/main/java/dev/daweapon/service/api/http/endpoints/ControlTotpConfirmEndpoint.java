package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAudit;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.Totp;
import dev.daweapon.service.api.control.TotpStore;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Activates a pending TOTP secret once a code from the authenticator app matches it. */
public class ControlTotpConfirmEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }

    @Override
    public String getPath() {
        return "/control/totp/confirm";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            ControlAudit.refused(req, "totp-confirm", List.of(), String.valueOf(auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }

        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String code = ControlEnrolEndpoint.optString(body, "code", null);
        TotpStore.State state = TotpStore.load();
        if (state.pendingSecret() == null) {
            res.status(400);
            return APIResponse.error("No enrolment in progress - start one first");
        }
        if (!Totp.verify(state.pendingSecret(), code, Instant.now().getEpochSecond())) {
            ControlAudit.caller(auth.caller(), "totp-confirm", List.of(), AuditEntry.Outcome.DENIED,
                    "code did not match the pending secret");
            res.status(403);
            return APIResponse.error("Second factor code invalid or expired - check the app's clock and try again");
        }

        TotpStore.save(new TotpStore.State(state.pendingSecret(), true, auth.caller().device().playerName(),
                Instant.now(), null, null, null));
        Logger.warn("Second factor enrolled by {}", auth.caller().actor());
        ControlAudit.caller(auth.caller(), "totp-confirm", List.of(), AuditEntry.Outcome.ACCEPTED,
                "second factor enrolled; OWNER actions now require a code");
        return APIResponse.success("Second factor enrolled");
    }
}
