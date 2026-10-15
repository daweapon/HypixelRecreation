package dev.daweapon.service.api.http.endpoints;

import dev.daweapon.service.api.APIKeyDatabase;
import dev.daweapon.service.api.http.APIResponse;
import dev.daweapon.service.api.http.ApiRateLimiterHandler;
import dev.daweapon.service.api.http.EndpointType;
import dev.daweapon.service.api.http.SkyBlockEndpoint;
import dev.daweapon.type.generic.data.mongodb.UserDatabase;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;

// Public API HTTP endpoint that returns a user's profile list, gated by API key and rate limiting.
public class UserEndpoint extends SkyBlockEndpoint {
    @Override
    public EndpointType getType() {
        return EndpointType.CONSUMER;
    }

    @Override
    public String getPath() {
        return "/user";
    }

    @Override
    public @Nullable APIResponse handle(Map<String, String> headers, Request req, Response res) {
        String apiKey = headers.get("X-API-Key");

        if (apiKey == null) {
            return APIResponse.error("Missing required header X-API-Key");
        }

        APIKeyDatabase keyDatabase = new APIKeyDatabase(apiKey);
        if (keyDatabase.fetch() == null) {
            return APIResponse.error("Invalid API key");
        }

        // Check rate limit before processing the request
        if (!ApiRateLimiterHandler.getInstance().isRequestAllowed(apiKey)) {
            long remaining = ApiRateLimiterHandler.getInstance().getRemainingRequests(apiKey);
            return APIResponse.error("Rate limit exceeded. Daily request limit reached. Remaining requests: " + remaining);
        }

        String userUuid = headers.get("X-User-UUID");
        if (userUuid == null) {
            return APIResponse.error("Missing required header X-User-UUID");
        }

        try {
            UUID.fromString(userUuid);
        } catch (IllegalArgumentException e) {
            return APIResponse.error("Invalid X-User-UUID header");
        }

        UUID uuid = UUID.fromString(userUuid);
        UserDatabase userDatabase = new UserDatabase(uuid);

        return APIResponse.success(new Document(userDatabase.getProfiles().serialize()).toJson());
    }
}

