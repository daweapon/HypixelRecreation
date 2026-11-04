package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.PlayerAdmin;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.HttpMethod;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.UUID;

/**
 * Player mutations: set-rank, kick, ban, mute, unban, unmute, wipe, restore.
 *
 * OWNER only, every attempt audited. Bans and mutes go through the punishment service exactly as
 * the in-game /ban and /mute do, so the proxy enforces them the same way.
 */
public class ControlAdminPlayerActionEndpoint extends ControlAdminEndpoint {
    @Override
    public String getPath() {
        return "/control/admin/player/action";
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
        return "player-" + ControlEnrolEndpoint.optString(body, "action", "unknown");
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception {
        String action = ControlEnrolEndpoint.optString(body, "action", "");
        UUID uuid = PlayerAdmin.resolve(ControlEnrolEndpoint.optString(body, "uuid", ""));
        if (uuid == null) throw new IllegalArgumentException("Unknown player");

        String reason = ControlEnrolEndpoint.optString(body, "reason", "");
        long durationMillis = body.has("durationMinutes") && !body.get("durationMinutes").isJsonNull()
                ? body.get("durationMinutes").getAsLong() * 60_000L : 0L;

        Logger.warn("CONTROL PLAYER ACTION {} on {} requested by {}", action, uuid, caller.actor());
        PlayerAdmin.Outcome outcome;
        try {
            outcome = run(caller, action, uuid, body, reason, durationMillis);
        } catch (Exception e) {
            audit(caller, "player-" + action, List.of(uuid.toString()), false, String.valueOf(e.getMessage()));
            throw e;
        }

        audit(caller, "player-" + action, List.of(uuid.toString()), outcome.ok(), outcome.detail());
        if (!outcome.ok()) res.status(409);
        return outcome.ok() ? ok(outcome) : APIResponse.error(outcome.detail());
    }

    private static PlayerAdmin.Outcome run(ControlAuth.Caller caller, String action, UUID uuid, JsonObject body,
                                           String reason, long durationMillis) throws Exception {
        return switch (action) {
            case "set-rank" -> PlayerAdmin.setRank(uuid, ControlEnrolEndpoint.optString(body, "rank", ""));
            case "kick" -> PlayerAdmin.kick(uuid, reason);
            case "ban" -> PlayerAdmin.punish(uuid, "BAN", reason, durationMillis, staffUuid(caller));
            case "mute" -> PlayerAdmin.punish(uuid, "MUTE", reason, durationMillis, staffUuid(caller));
            case "unban" -> PlayerAdmin.unpunish(uuid, "BAN", staffUuid(caller));
            case "unmute" -> PlayerAdmin.unpunish(uuid, "MUTE", staffUuid(caller));
            case "wipe" -> PlayerAdmin.wipe(uuid);
            case "restore" -> PlayerAdmin.restore(uuid);
            default -> throw new IllegalArgumentException("Unknown action '" + action
                    + "' - expected set-rank, kick, ban, mute, unban, unmute, wipe or restore");
        };
    }
}
