package dev.daweapon.service.api.control;

import org.tinylog.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Talks to the control agent.
 *
 * service.api never touches the Docker socket itself; it authenticates the caller, checks their
 * role, and forwards. That keeps the process serving public CONSUMER traffic away from
 * root-equivalent control of the host.
 */
public final class ControlAgentClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final String baseUrl;
    private final String token;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Default host is the hyphenated network alias, NOT the container name.
     *
     * The container is called service_control, but Java's URI parser follows RFC 2396, where
     * an underscore is not legal in a hostname: URI.getHost() returns null and HttpRequest
     * rejects the whole thing with "unsupported URI" before a single packet is sent. curl and
     * most other clients are lenient about this, so the fault presents as the agent being
     * unreachable while every manual connectivity check passes. The compose override gives the
     * agent an underscore-free alias for exactly this reason.
     */
    private static final String DEFAULT_AGENT_URL = "http://service-control:9090";

    public ControlAgentClient() {
        this(System.getenv().getOrDefault("CONTROL_AGENT_URL", DEFAULT_AGENT_URL),
                System.getenv("CONTROL_TOKEN"));
    }

    public ControlAgentClient(String baseUrl, String token) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.token = token;
        warnIfHostUnusable(this.baseUrl);
    }

    /**
     * Fails loudly at startup rather than once per request. A host Java cannot parse is a
     * configuration error, and reporting it as "unreachable" at call time sends whoever is
     * debugging it hunting through container networking instead of the URL.
     */
    private static void warnIfHostUnusable(String baseUrl) {
        try {
            if (URI.create(baseUrl).getHost() == null) {
                Logger.error("CONTROL_AGENT_URL '{}' has no host Java can parse - an underscore "
                        + "in the hostname does this. Use an underscore-free network alias.", baseUrl);
            }
        } catch (IllegalArgumentException e) {
            Logger.error("CONTROL_AGENT_URL '{}' is not a valid URI: {}", baseUrl, e.getMessage());
        }
    }

    public record AgentResponse(int status, String body) {
        public boolean isSuccess() {
            return status >= 200 && status < 300;
        }
    }

    public boolean isConfigured() {
        return token != null && !token.isBlank();
    }

    public AgentResponse get(String path, String actor) {
        return send(HttpRequest.newBuilder().GET(), path, actor);
    }

    public AgentResponse post(String path, String jsonBody, String actor) {
        return send(HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "{}" : jsonBody)), path, actor);
    }

    private AgentResponse send(HttpRequest.Builder builder, String path, String actor) {
        try {
            HttpRequest request = builder
                    .uri(URI.create(baseUrl + path))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + (token == null ? "" : token))
                    .header("X-Actor", actor == null ? "unknown" : actor)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new AgentResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            // The agent being unreachable is itself a reportable condition, not a 500 with no
            // explanation - the app renders this as "control plane unreachable".
            return new AgentResponse(503, "{\"error\":\"control agent unreachable: "
                    + String.valueOf(e.getMessage()).replace('"', '\'') + "\"}");
        }
    }
}
