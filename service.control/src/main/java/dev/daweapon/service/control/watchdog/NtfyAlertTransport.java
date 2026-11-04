package dev.daweapon.service.control.watchdog;

import dev.daweapon.service.control.http.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Publishes alerts to an ntfy topic, formatted for a phone notification: a title, a priority
 * that decides whether the phone buzzes, and emoji tags so severity reads at a glance.
 *
 * Uses ntfy's JSON publishing (POST to the server root with the topic in the body) rather than
 * the header form, because Java's HTTP client rejects non-ASCII header values and container
 * names or exception text can contain them.
 *
 * Configured with ALERT_NTFY_URL (the full topic URL, e.g. https://ntfy.sh/my-private-topic)
 * and optionally ALERT_NTFY_TOKEN for a protected topic.
 */
public final class NtfyAlertTransport implements AlertTransport {
    public record Target(String baseUrl, String topic) {
    }

    public record Message(String title, String body, int priority, List<String> tags) {
    }

    private final Target target;
    private final String token;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public NtfyAlertTransport(String topicUrl, String token) {
        this.target = target(topicUrl);
        this.token = token == null || token.isBlank() ? null : token.trim();
    }

    public static Target target(String topicUrl) {
        String trimmed = topicUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        int slash = trimmed.lastIndexOf('/');
        if (slash < 0 || slash < trimmed.indexOf("://") + 3 || slash == trimmed.length() - 1) {
            throw new IllegalArgumentException("ALERT_NTFY_URL must be a topic URL such as "
                    + "https://ntfy.sh/my-topic, got '" + topicUrl + "'");
        }
        return new Target(trimmed.substring(0, slash), trimmed.substring(slash + 1));
    }

    public static Message message(Alert alert) {
        List<String> tags = new ArrayList<>();
        int priority;
        switch (alert.severity()) {
            case CRITICAL -> {
                priority = 5;
                tags.add("rotating_light");
            }
            case WARNING -> {
                priority = 4;
                tags.add("warning");
            }
            case RECOVERY -> {
                priority = 3;
                tags.add("white_check_mark");
            }
            default -> {
                priority = 3;
                tags.add("information_source");
            }
        }
        String key = alert.key() == null ? "" : alert.key();
        int colon = key.indexOf(':');
        if (colon > 0) {
            tags.add(key.substring(0, colon));
        }
        String body = alert.body() == null || alert.body().isBlank() ? alert.title() : alert.body();
        return new Message(alert.title(), body, priority, tags);
    }

    @Override
    public String name() {
        return "ntfy";
    }

    @Override
    public void send(Alert alert) throws Exception {
        Message message = message(alert);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("topic", target.topic());
        payload.put("title", message.title());
        payload.put("message", message.body());
        payload.put("priority", message.priority());
        payload.put("tags", message.tags());

        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(target.baseUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.write(payload)));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("ntfy returned HTTP " + response.statusCode() + ": " + response.body());
        }
    }
}
