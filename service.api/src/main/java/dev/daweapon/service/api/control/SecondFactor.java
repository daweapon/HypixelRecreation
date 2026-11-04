package dev.daweapon.service.api.control;

import dev.daweapon.service.api.http.APIResponse;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.List;

/**
 * Optional TOTP second factor on OWNER actions.
 *
 * Nothing changes until an owner enrols a secret: with no active enrolment every check passes.
 * Once enrolled, deploy, restore, role changes, backup settings, reconcile and owner recovery
 * need a current code in the {@code X-Control-TOTP} header (or a {@code totp} body field where a
 * route has no bearer token). A missing or wrong code is refused and audited as DENIED.
 *
 * Codes are not single-use: they are accepted for their 30-second step plus one either side,
 * so one code can cover two actions in quick succession. A code alone is useless without the
 * device's bearer token, and the bearer token alone is now useless without the code.
 */
public final class SecondFactor {
    public static final String HEADER = "X-Control-TOTP";

    public enum Verdict {
        NOT_ENROLLED,
        OK,
        MISSING,
        INVALID
    }

    private SecondFactor() {
    }

    public static Verdict evaluate(TotpStore.State state, @Nullable String code, long epochSeconds) {
        if (state == null || !state.active() || state.secret() == null) {
            return Verdict.NOT_ENROLLED;
        }
        if (code == null || code.isBlank()) {
            return Verdict.MISSING;
        }
        return Totp.verify(state.secret(), code, epochSeconds) ? Verdict.OK : Verdict.INVALID;
    }

    public static boolean isEnrolled() {
        return TotpStore.load().active();
    }

    /**
     * Checks the code for an authorised OWNER request. Returns null to proceed, or the error to
     * return; a refusal is already audited and the response status set.
     */
    public static @Nullable APIResponse require(Request req, Response res, ControlAuth.Caller caller,
                                                String action, List<String> targets) {
        return require(req.headers(HEADER), res, caller.device().playerName(), caller.device().deviceId(),
                caller.device().deviceName(), caller.role().name(), action, targets);
    }

    public static @Nullable APIResponse require(@Nullable String code, Response res, String actorName,
                                                String deviceId, String deviceName, String role,
                                                String action, List<String> targets) {
        Verdict verdict;
        try {
            verdict = evaluate(TotpStore.load(), code, Instant.now().getEpochSecond());
        } catch (Exception e) {
            // Fail closed: an unreadable enrolment must not silently switch the factor off.
            Logger.error(e, "Could not read the TOTP enrolment");
            res.status(503);
            return APIResponse.error("Second factor store unavailable - refusing " + action);
        }

        if (verdict == Verdict.NOT_ENROLLED || verdict == Verdict.OK) {
            return null;
        }
        String reason = verdict == Verdict.MISSING
                ? "Second factor required - enter the code from your authenticator app"
                : "Second factor code invalid or expired";
        AuditDatabase.record(AuditEntry.of(actorName, deviceId, deviceName, role, action, targets,
                AuditEntry.Outcome.DENIED, reason));
        res.status(403);
        return APIResponse.error(reason);
    }
}
