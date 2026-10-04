package net.swofty.service.control.watchdog;

import net.swofty.service.control.http.Json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Posts alerts as JSON to a configured URL.
 *
 * This exists so push alerting works before the Flutter client does: an FCM device token can
 * only come from an installed app, whereas a webhook target (ntfy, Gotify, Discord) delivers to
 * a phone today with no credentials to provision. Configured with ALERT_WEBHOOK_URL.
 */
public final class WebhookAlertTransport implements AlertTransport {
    private final String url;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public WebhookAlertTransport(String url) {
        this.url = url;
    }

    @Override
    public String name() {
        return "webhook";
    }

    @Override
    public void send(Alert alert) throws Exception {
        String payload = Json.write(Map.of(
                "severity", alert.severity().name(),
                "title", alert.title(),
                "message", alert.body(),
                "key", alert.key(),
                "at", alert.at().toString()
        ));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Title", alert.title())
                .header("Priority", alert.severity() == Alert.Severity.CRITICAL ? "urgent" : "default")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Webhook returned HTTP " + response.statusCode());
        }
    }
}
