package net.swofty.service.api.http.endpoints;

import com.google.gson.JsonObject;
import net.swofty.service.api.control.AuditDatabase;
import net.swofty.service.api.control.AuditEntry;
import net.swofty.service.api.control.ControlAuth;
import net.swofty.service.api.control.ControlRole;
import net.swofty.service.api.control.ControlTokens;
import net.swofty.service.api.control.DeviceDatabase;
import net.swofty.service.api.control.DeviceRecord;
import net.swofty.service.api.http.APIResponse;
import net.swofty.service.api.http.EndpointType;
import net.swofty.service.api.http.HttpMethod;
import net.swofty.service.api.http.SkyBlockEndpoint;
import org.tinylog.Logger;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

/**
 * Promotes, demotes or revokes a device. Owner only.
 *
 * An owner cannot revoke or demote their own device: doing so could leave the install with no
 * owner at all and no way back in short of editing Mongo by hand.
 */
public class ControlDeviceUpdateEndpoint extends SkyBlockEndpoint {
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
        return "/control/devices/update";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            res.status(403);
            return auth.error();
        }

        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String deviceId = ControlEnrolEndpoint.optString(body, "deviceId", null);
        String action = ControlEnrolEndpoint.optString(body, "action", "");

        if (deviceId == null || deviceId.isBlank()) {
            res.status(400);
            return APIResponse.error("Missing deviceId");
        }

        DeviceRecord device = DeviceDatabase.findByDeviceId(deviceId);
        if (device == null) {
            res.status(404);
            return APIResponse.error("No such device");
        }

        boolean self = deviceId.equals(auth.caller().accessToken().deviceId());

        switch (action) {
            case "revoke" -> {
                if (self) {
                    res.status(400);
                    return APIResponse.error("Refusing to revoke the device making this request");
                }
                DeviceDatabase.save(device.revokedCopy());
                ControlTokens.revokeDevice(deviceId);
                Logger.warn("Device {} revoked by {}", deviceId, auth.caller().actor());
                AuditDatabase.record(AuditEntry.of(
                        auth.caller().device().playerName(), auth.caller().device().deviceId(),
                        auth.caller().device().deviceName(), auth.caller().role().name(),
                        "device-revoke", List.of(deviceId), AuditEntry.Outcome.ACCEPTED,
                        "revoked device '" + device.deviceName() + "' of " + device.playerName()));
                return APIResponse.success("Device revoked");
            }
            case "role" -> {
                ControlRole newRole = ControlRole.parse(
                        ControlEnrolEndpoint.optString(body, "role", null), null);
                if (newRole == null) {
                    res.status(400);
                    return APIResponse.error("Missing or invalid role");
                }
                if (self && newRole != ControlRole.OWNER) {
                    res.status(400);
                    return APIResponse.error("Refusing to demote the device making this request");
                }
                DeviceDatabase.save(device.withRole(newRole));
                ControlTokens.revokeDevice(deviceId);
                Logger.warn("Device {} set to {} by {}", deviceId, newRole, auth.caller().actor());
                AuditDatabase.record(AuditEntry.of(
                        auth.caller().device().playerName(), auth.caller().device().deviceId(),
                        auth.caller().device().deviceName(), auth.caller().role().name(),
                        "device-role", List.of(deviceId), AuditEntry.Outcome.ACCEPTED,
                        "changed '" + device.deviceName() + "' from " + device.role()
                                + " to " + newRole));
                return APIResponse.success("Device role updated to " + newRole);
            }
            default -> {
                res.status(400);
                return APIResponse.error("Unknown action '" + action + "' - expected revoke or role");
            }
        }
    }
}
