package net.swofty.service.control.docker;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Minimal HTTP/1.1 client speaking the Docker Engine API over a Unix domain socket.
 *
 * Hand-rolled rather than pulling in a Docker client library on purpose: this service exists to
 * report on the stack when the stack is broken, so its dependency surface is kept as small as
 * possible. Only the handful of verbs the control plane actually needs are supported.
 */
public final class DockerSocketClient {
    private static final String DEFAULT_SOCKET = "/var/run/docker.sock";

    /**
     * Hard ceiling on a single Docker call.
     *
     * A blocking read on a Unix domain SocketChannel has no timeout of its own, so without
     * this a single unresponsive call would hang the calling thread forever - and because a
     * fleet snapshot inspects every container, one hung inspect would stall the whole
     * snapshot and surface to the operator as an unexplained timeout further upstream.
     */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /** Uploading a ~50MB jar legitimately takes longer than a status call. */
    private static final Duration UPLOAD_TIMEOUT = Duration.ofSeconds(120);

    /** Closes channels that overrun REQUEST_TIMEOUT; the blocked read then fails fast. */
    private static final ScheduledExecutorService TIMEOUT_WATCHDOG =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "docker-socket-timeout");
                thread.setDaemon(true);
                return thread;
            });

    private final Path socketPath;

    public DockerSocketClient() {
        this(Path.of(System.getenv().getOrDefault("DOCKER_SOCKET", DEFAULT_SOCKET)));
    }

    public DockerSocketClient(Path socketPath) {
        this.socketPath = socketPath;
    }

    public Path getSocketPath() {
        return socketPath;
    }

    /** True when the socket file exists and is readable; checked before every call so a missing
     *  mount produces a clear diagnosis instead of an opaque IO error. */
    public boolean isAvailable() {
        return Files.exists(socketPath) && Files.isReadable(socketPath);
    }

    public DockerResponse get(String path) {
        return request("GET", path, null);
    }

    public DockerResponse post(String path) {
        return request("POST", path, null);
    }

    /**
     * POSTs with an explicit time budget.
     *
     * Lifecycle calls carry their own grace period (`?t=`), and Docker does not answer until
     * that has elapsed. A fixed client ceiling below it therefore reports a timeout for every
     * container that uses its full allowance, even though the restart succeeded.
     */
    public DockerResponse post(String path, Duration budget) {
        return send("POST", path, null, null, budget);
    }

    /** PUTs a raw body (used for the tar stream that implements `docker cp`). */
    public DockerResponse putBinary(String path, byte[] body, String contentType) {
        return send("PUT", path, body, contentType);
    }

    public DockerResponse request(String method, String path, String jsonBody) {
        return send(method, path,
                jsonBody == null ? null : jsonBody.getBytes(StandardCharsets.UTF_8),
                jsonBody == null ? null : "application/json");
    }

    private DockerResponse send(String method, String path, byte[] body, String contentType) {
        return send(method, path, body, contentType, null);
    }

    private DockerResponse send(String method, String path, byte[] body, String contentType,
                                Duration explicitBudget) {
        if (!isAvailable()) {
            throw new DockerException("Docker socket is not available at " + socketPath
                    + " - is /var/run/docker.sock mounted into this container?");
        }

        AtomicBoolean timedOut = new AtomicBoolean(false);
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            Duration budget = explicitBudget != null ? explicitBudget
                    : body != null && body.length > 1_000_000 ? UPLOAD_TIMEOUT : REQUEST_TIMEOUT;
            ScheduledFuture<?> watchdog = TIMEOUT_WATCHDOG.schedule(() -> {
                timedOut.set(true);
                try {
                    // Closing the channel unblocks any read stuck on it.
                    channel.close();
                } catch (IOException ignored) {
                    // Nothing useful to do; the caller already sees the failure.
                }
            }, budget.toMillis(), TimeUnit.MILLISECONDS);

            try {
                channel.connect(UnixDomainSocketAddress.of(socketPath));

                OutputStream out = Channels.newOutputStream(channel);
                writeRequest(out, method, path, body, contentType);
                out.flush();

                return readResponse(new BufferedInputStream(Channels.newInputStream(channel)));
            } finally {
                watchdog.cancel(false);
            }
        } catch (IOException e) {
            if (timedOut.get()) {
                throw new DockerException("Docker Engine API call timed out: "
                        + method + " " + path, e);
            }
            throw new DockerException("Docker Engine API call failed: " + method + " " + path, e);
        }
    }

    private void writeRequest(OutputStream out, String method, String path,
                              byte[] payload, String contentType) throws IOException {
        StringBuilder head = new StringBuilder();
        head.append(method).append(' ').append(path).append(" HTTP/1.1\r\n");
        head.append("Host: docker\r\n");
        head.append("Accept: application/json\r\n");
        head.append("Connection: close\r\n");

        if (payload != null) {
            head.append("Content-Type: ")
                    .append(contentType == null ? "application/octet-stream" : contentType)
                    .append("\r\n");
            head.append("Content-Length: ").append(payload.length).append("\r\n");
        }
        head.append("\r\n");

        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
        if (payload != null) {
            out.write(payload);
        }
    }

    private DockerResponse readResponse(InputStream in) throws IOException {
        String statusLine = readLine(in);
        if (statusLine == null || statusLine.isEmpty()) {
            throw new DockerException("Docker returned an empty response");
        }
        String[] statusParts = statusLine.split(" ", 3);
        if (statusParts.length < 2) {
            throw new DockerException("Malformed status line from Docker: " + statusLine);
        }
        int status = Integer.parseInt(statusParts[1].trim());

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
            }
        }

        byte[] body = readBody(in, headers);
        return new DockerResponse(status, body);
    }

    private byte[] readBody(InputStream in, Map<String, String> headers) throws IOException {
        String transferEncoding = headers.get("transfer-encoding");
        if (transferEncoding != null && transferEncoding.toLowerCase().contains("chunked")) {
            return readChunked(in);
        }

        String contentLength = headers.get("content-length");
        if (contentLength != null) {
            int length = Integer.parseInt(contentLength.trim());
            return in.readNBytes(length);
        }

        // No framing headers: the server closes the connection to signal the end of the body.
        return in.readAllBytes();
    }

    private byte[] readChunked(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            String sizeLine = readLine(in);
            if (sizeLine == null) {
                break;
            }
            // A chunk size may carry trailing extensions after a semicolon.
            int semicolon = sizeLine.indexOf(';');
            if (semicolon >= 0) {
                sizeLine = sizeLine.substring(0, semicolon);
            }
            sizeLine = sizeLine.trim();
            if (sizeLine.isEmpty()) {
                continue;
            }

            int chunkSize = Integer.parseInt(sizeLine, 16);
            if (chunkSize == 0) {
                break;
            }
            buffer.write(in.readNBytes(chunkSize));
            readLine(in);
        }
        return buffer.toByteArray();
    }

    /** Reads a single CRLF-terminated line. Returns null at end of stream. */
    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        boolean any = false;
        while ((read = in.read()) != -1) {
            any = true;
            if (read == '\n') {
                break;
            }
            if (read != '\r') {
                buffer.write(read);
            }
        }
        if (!any && read == -1) {
            return null;
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
