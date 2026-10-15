package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.service.api.control.ControlTokens;
import dev.daweapon.service.api.control.DeviceDatabase;
import dev.daweapon.service.api.control.DeviceRecord;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.HttpMethod;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import spark.Request;
import spark.Response;

import java.time.Instant;
import java.util.Map;

/** Exchanges a device token for a short-lived access token. */
public class ControlTokenEndpoint extends SkyBlockEndpoint {
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
        return "/control/token";
    }

    @Override
    public APIResponse handle(Map<String, String> headers, Request req, Response res) {
        JsonObject body = ControlEnrolEndpoint.parseBody(req);
        String deviceToken = ControlEnrolEndpoint.optString(body, "deviceToken",
                headers.get("X-Device-Token"));

        if (deviceToken == null || deviceToken.isBlank()) {
            return APIResponse.error("Missing deviceToken");
        }

        DeviceRecord device = DeviceDatabase.findByTokenHash(ControlTokens.hash(deviceToken));
        if (device == null) {
            return APIResponse.error("Unknown device token");
        }
        if (device.revoked()) {
            return APIResponse.error("Device has been revoked");
        }

        DeviceDatabase.save(device.withLastSeen(Instant.now()));
        String accessToken = ControlTokens.issueAccessToken(device);

        JsonObject payload = new JsonObject();
        payload.addProperty("accessToken", accessToken);
        payload.addProperty("expiresInSeconds", ControlTokens.ACCESS_TOKEN_TTL.toSeconds());
        payload.addProperty("role", device.role().name());
        payload.addProperty("deviceId", device.deviceId());
        payload.addProperty("playerName", device.playerName());
        return APIResponse.success(payload.toString());
    }
}
