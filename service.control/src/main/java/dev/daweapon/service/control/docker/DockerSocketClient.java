package dev.daweapon.service.control.docker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

    /** POSTs a JSON body with an explicit budget; an exec's output arrives only once it ends. */
    public DockerResponse post(String path, String jsonBody, Duration budget) {
        return send("POST", path, jsonBody.getBytes(StandardCharsets.UTF_8), "application/json", budget);
    }

    /** PUTs a raw body (used for the tar stream that implements `docker cp`). */
    public DockerResponse putBinary(String path, byte[] body, String contentType) {
        return send("PUT", path, body, contentType);
    }

    public DockerResponse putBinary(String path, byte[] body, String contentType, Duration budget) {
        return send("PUT", path, body, contentType, budget);
    }

    public DockerResponse request(String method, String path, String jsonBody) {
        return send(method, path,
                jsonBody == null ? null : jsonBody.getBytes(StandardCharsets.UTF_8),
                jsonBody == null ? null : "application/json");
    }

    /** A JSON request with its own time budget, for calls that legitimately run long (exec). */
    public DockerResponse request(String method, String path, String jsonBody, Duration budget) {
        return send(method, path,
                jsonBody == null ? null : jsonBody.getBytes(StandardCharsets.UTF_8),
                jsonBody == null ? null : "application/json", budget);
    }

    /** Writes a request body of known length straight to the socket. */
    public interface BodyWriter {
        void writeTo(OutputStream out) throws IOException;
    }

    /**
     * PUTs a streamed body without holding it in memory, so a database dump of any size can be
     * pushed into a container.
     */
    public DockerResponse putStream(String path, long length, BodyWriter writer, String contentType,
                                    Duration budget) {
        return exchange("PUT", path, budget, out -> {
            writeHead(out, "PUT", path, contentType, length);
            writer.writeTo(out);
        }, this::readResponse);
    }

    /**
     * GETs a body straight into a file. Used to copy database dumps out of a container through
     * the archive endpoint, where the body can be far larger than is sensible to buffer.
     */
    public void download(String path, Path destination, Duration budget) {
        exchange("GET", path, budget, out -> writeHead(out, "GET", path, null, -1), in -> {
            ResponseHead head = readHead(in);
            if (head.status() < 200 || head.status() >= 300) {
                ByteArrayOutputStream error = new ByteArrayOutputStream();
                copyBody(in, head.headers(), error);
                throw new DockerException("GET " + path + " failed with HTTP " + head.status()
                        + ": " + error.toString(StandardCharsets.UTF_8));
            }
            try (OutputStream file = Files.newOutputStream(destination)) {
                copyBody(in, head.headers(), file);
            }
            return null;
        });
    }

    private DockerResponse send(String method, String path, byte[] body, String contentType) {
        return send(method, path, body, contentType, null);
    }

    private DockerResponse send(String method, String path, byte[] body, String contentType,
                                Duration explicitBudget) {
        Duration budget = explicitBudget != null ? explicitBudget
                : body != null && body.length > 1_000_000 ? UPLOAD_TIMEOUT : REQUEST_TIMEOUT;
        return exchange(method, path, budget, out -> {
            writeHead(out, method, path, body == null ? null : contentType, body == null ? -1 : body.length);
            if (body != null) {
                out.write(body);
            }
        }, this::readResponse);
    }

    private interface ResponseReader<T> {
        T read(InputStream in) throws IOException;
    }

    private <T> T exchange(String method, String path, Duration budget, BodyWriter request,
                           ResponseReader<T> reader) {
        if (!isAvailable()) {
            throw new DockerException("Docker socket is not available at " + socketPath
                    + " - is /var/run/docker.sock mounted into this container?");
        }

        AtomicBoolean timedOut = new AtomicBoolean(false);
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
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

                OutputStream out = new BufferedOutputStream(Channels.newOutputStream(channel), 65536);
                request.writeTo(out);
                out.flush();

                return reader.read(new BufferedInputStream(Channels.newInputStream(channel), 65536));
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

    /** Writes the request line and headers; a negative length means no body. */
    private void writeHead(OutputStream out, String method, String path,
                           String contentType, long length) throws IOException {
        StringBuilder head = new StringBuilder();
        head.append(method).append(' ').append(path).append(" HTTP/1.1\r\n");
        head.append("Host: docker\r\n");
        head.append("Accept: application/json\r\n");
        head.append("Connection: close\r\n");

        if (length >= 0) {
            head.append("Content-Type: ")
                    .append(contentType == null ? "application/octet-stream" : contentType)
                    .append("\r\n");
            head.append("Content-Length: ").append(length).append("\r\n");
        }
        head.append("\r\n");

        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
    }

    private record ResponseHead(int status, Map<String, String> headers) {
    }

    private DockerResponse readResponse(InputStream in) throws IOException {
        ResponseHead head = readHead(in);
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        copyBody(in, head.headers(), body);
        return new DockerResponse(head.status(), body.toByteArray());
    }

    private ResponseHead readHead(InputStream in) throws IOException {
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

        return new ResponseHead(status, headers);
    }

    private void copyBody(InputStream in, Map<String, String> headers, OutputStream out) throws IOException {
        String transferEncoding = headers.get("transfer-encoding");
        if (transferEncoding != null && transferEncoding.toLowerCase().contains("chunked")) {
            copyChunked(in, out);
            return;
        }

        String contentLength = headers.get("content-length");
        if (contentLength != null) {
            copyExactly(in, out, Long.parseLong(contentLength.trim()));
            return;
        }

        // No framing headers: the server closes the connection to signal the end of the body.
        in.transferTo(out);
    }

    private static void copyExactly(InputStream in, OutputStream out, long length) throws IOException {
        byte[] buffer = new byte[65536];
        long remaining = length;
        while (remaining > 0) {
            int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read < 0) {
                break;
            }
            out.write(buffer, 0, read);
            remaining -= read;
        }
    }

    private void copyChunked(InputStream in, OutputStream buffer) throws IOException {
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

            long chunkSize = Long.parseLong(sizeLine, 16);
            if (chunkSize == 0) {
                break;
            }
            copyExactly(in, buffer, chunkSize);
            readLine(in);
        }
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
