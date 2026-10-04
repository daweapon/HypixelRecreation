package net.swofty.service.api.http;

import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;

import java.util.Map;

// Base class for a public HTTP API endpoint: declares its path, access type, and request handling.
public abstract class SkyBlockEndpoint {
    public abstract EndpointType getType();
    public abstract String getPath();

    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }

    public abstract @Nullable APIResponse handle(Map<String, String> headers, Request req, Response res);
}
