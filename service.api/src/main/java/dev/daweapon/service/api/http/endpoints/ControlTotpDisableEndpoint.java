package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAudit;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.SecondFactor;
import dev.daweapon.service.api.control.TotpStore;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

/** Removes the second factor. Needs a current code, so a stolen owner device alone cannot. */
public class ControlTotpDisableEndpoint extends SkyBlockEndpoint {
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
        return "/control/totp/disable";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            ControlAudit.refused(req, "totp-disable", List.of(), String.valueOf(auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }
        if (!SecondFactor.isEnrolled()) {
            TotpStore.clear();
            return APIResponse.success("No second factor was enrolled");
        }
        APIResponse refusal = SecondFactor.require(req, res, auth.caller(), "totp-disable", List.of());
        if (refusal != null) {
            return refusal;
        }

        TotpStore.clear();
        Logger.warn("Second factor removed by {}", auth.caller().actor());
        ControlAudit.caller(auth.caller(), "totp-disable", List.of(), AuditEntry.Outcome.ACCEPTED,
                "second factor removed; OWNER actions no longer require a code");
        return APIResponse.success("Second factor removed");
    }
}
