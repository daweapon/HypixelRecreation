package net.swofty.service.api.control;

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

    public ControlAgentClient() {
        this(System.getenv().getOrDefault("CONTROL_AGENT_URL", "http://service_control:9090"),
                System.getenv("CONTROL_TOKEN"));
    }

    public ControlAgentClient(String baseUrl, String token) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.token = token;
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
