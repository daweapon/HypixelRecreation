package dev.daweapon.service.control.integrity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Small HTTP checks against other containers and public URLs.
 *
 * Containers are addressed by IP from inspect rather than by name: every container in this
 * stack is named with an underscore, which Java's URI parser rejects as a hostname.
 */
public final class HttpProbe {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** @param status the HTTP status, or -1 when nothing answered */
    public record Outcome(String url, int status, String error) {
        public boolean answered() {
            return status > 0;
        }

        public boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    private HttpProbe() {
    }

    /** Falls back to a GET that reads only the headers, for servers that drop HEAD requests (the pack server does). */
    public static Outcome head(String url, Duration timeout) {
        Outcome head = send(url, "HEAD", timeout);
        return head.answered() ? head : headersOnly(url, timeout);
    }

    public static Outcome get(String url, Duration timeout) {
        return send(url, "GET", timeout);
    }

    private static Outcome send(String url, String method, Duration timeout) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .method(method, HttpRequest.BodyPublishers.noBody())
                    .timeout(timeout)
                    .build();
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            return new Outcome(url, response.statusCode(), null);
        } catch (Exception e) {
            return new Outcome(url, -1, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static Outcome headersOnly(String url, Duration timeout) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().timeout(timeout).build();
            HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            response.body().close();
            return new Outcome(url, response.statusCode(), null);
        } catch (Exception e) {
            return new Outcome(url, -1, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Downloads a body and returns its digest in the given algorithm, or null on any failure.
     * The whole exchange, body included, is bounded by the timeout.
     */
    public static String digestOf(String url, String algorithm, Duration timeout) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().timeout(timeout).build();
            HttpResponse<byte[]> response = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (response.statusCode() != 200) {
                return null;
            }
            try (InputStream body = new ByteArrayInputStream(response.body())) {
                return HashCache.digest(body, algorithm);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
