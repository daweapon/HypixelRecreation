package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.DeviceDatabase;
import dev.daweapon.service.api.control.DeviceRecord;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.util.Map;

/** Lists enrolled devices. Owner only - this is the roster of who can control the stack. */
public class ControlDevicesEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/control/devices";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        ControlAuth.Result auth = ControlAuth.authorise(req, ControlRole.OWNER);
        if (auth.failed()) {
            res.status(403);
            return auth.error();
        }

        JsonArray devices = new JsonArray();
        for (DeviceRecord device : DeviceDatabase.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("deviceId", device.deviceId());
            entry.addProperty("deviceName", device.deviceName());
            entry.addProperty("playerName", device.playerName());
            entry.addProperty("role", device.role().name());
            entry.addProperty("createdAt", device.createdAt().toString());
            entry.addProperty("lastSeenAt", device.lastSeenAt() == null
                    ? null : device.lastSeenAt().toString());
            entry.addProperty("revoked", device.revoked());
            devices.add(entry);
        }
        return APIResponse.success(devices.toString());
    }
}
