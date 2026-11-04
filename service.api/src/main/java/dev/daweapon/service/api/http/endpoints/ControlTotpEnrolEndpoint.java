package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.AuditEntry;
import dev.daweapon.service.api.control.ControlAudit;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.SecondFactor;
import dev.daweapon.service.api.control.Totp;
import dev.daweapon.service.api.control.TotpStore;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Starts TOTP enrolment: generates a secret and returns it as an otpauth URI and as text to
 * type into an authenticator app. Nothing is enforced until {@code /control/totp/confirm}
 * proves the app produces matching codes. Replacing an active secret needs a current code.
 */
public class ControlTotpEnrolEndpoint extends SkyBlockEndpoint {
    private static final String ISSUER = "Hypixel Control";

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
        return "/control/totp/enrol";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            ControlAudit.refused(req, "totp-enrol", List.of(), String.valueOf(auth.error().getMessage()));
            res.status(403);
            return auth.error();
        }
        APIResponse refusal = SecondFactor.require(req, res, auth.caller(), "totp-enrol", List.of());
        if (refusal != null) {
            return refusal;
        }

        String secret = Totp.newSecret();
        String account = auth.caller().device().playerName();
        TotpStore.State current = TotpStore.load();
        TotpStore.save(new TotpStore.State(current.secret(), current.active(), current.enrolledBy(),
                current.enrolledAt(), secret, account, Instant.now()));
        ControlAudit.caller(auth.caller(), "totp-enrol", List.of(), AuditEntry.Outcome.ACCEPTED,
                "new second-factor secret generated; not enforced until confirmed");

        JsonObject payload = new JsonObject();
        payload.addProperty("secret", secret);
        payload.addProperty("otpauthUri", Totp.otpauthUri(secret, account, ISSUER));
        payload.addProperty("issuer", ISSUER);
        payload.addProperty("account", account);
        payload.addProperty("digits", Totp.DIGITS);
        payload.addProperty("periodSeconds", Totp.PERIOD_SECONDS);
        return new APIResponse(StatusResponse.SUCCESS, payload);
    }
}
