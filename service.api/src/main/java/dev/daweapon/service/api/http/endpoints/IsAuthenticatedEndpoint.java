package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.APIAdminDatabase;
import dev.daweapon.service.api.APIAdminDatabaseObject;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.service.api.http.StatusResponse;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;

import java.util.Map;

// Admin panel HTTP endpoint that reports whether the caller's session cookie is authenticated.
public class IsAuthenticatedEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.ADMINISTRATIVE;
    }

    @Override
    public String getPath() {
        return "/isAuthenticated";
    }

    @Override
    public @Nullable APIResponse handle(Map<String, String> headers, Request req, Response res) {
        String sessionId = req.cookie("sessionId");

        if (sessionId == null) {
            return APIResponse.error("Missing required headers");
        }

        APIAdminDatabase adminDatabase = new APIAdminDatabase();
        APIAdminDatabaseObject document = adminDatabase.getFromSessionId(sessionId);

        if (document == null) {
            return APIResponse.error("Invalid session ID");
        }

        if (!document.isAuthenticated()) {
            return APIResponse.error("Session is not authenticated");
        }

        return new APIResponse(StatusResponse.SUCCESS, document.toDocument().toJson());
    }
}
