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
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal HTTP/1.1 client speaking the Docker Engine API over a Unix domain socket.
 *
 * Hand-rolled rather than pulling in a Docker client library on purpose: this service exists to
 * report on the stack when the stack is broken, so its dependency surface is kept as small as
 * possible. Only the handful of verbs the control plane actually needs are supported.
 */
public final class DockerSocketClient {
    private static final String DEFAULT_SOCKET = "/var/run/docker.sock";
    private static final int CONNECT_TIMEOUT_MS = 5_000;

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

    public DockerResponse request(String method, String path, String jsonBody) {
        if (!isAvailable()) {
            throw new DockerException("Docker socket is not available at " + socketPath
                    + " - is /var/run/docker.sock mounted into this container?");
        }

        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(UnixDomainSocketAddress.of(socketPath));

            OutputStream out = Channels.newOutputStream(channel);
            writeRequest(out, method, path, jsonBody);
            out.flush();

            return readResponse(new BufferedInputStream(Channels.newInputStream(channel)));
        } catch (IOException e) {
            throw new DockerException("Docker Engine API call failed: " + method + " " + path, e);
        }
    }

    private void writeRequest(OutputStream out, String method, String path, String jsonBody) throws IOException {
        StringBuilder head = new StringBuilder();
        head.append(method).append(' ').append(path).append(" HTTP/1.1\r\n");
        head.append("Host: docker\r\n");
        head.append("Accept: application/json\r\n");
        head.append("Connection: close\r\n");

        byte[] payload = jsonBody == null ? null : jsonBody.getBytes(StandardCharsets.UTF_8);
        if (payload != null) {
            head.append("Content-Type: application/json\r\n");
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
