package net.swofty.service.control.docker;

import java.nio.charset.StandardCharsets;

/**
 * Raw result of a single Docker Engine API call.
 *
 * The body is kept as bytes because log streams are binary-framed; decoding to a String first
 * would corrupt the frame headers.
 */
public record DockerResponse(int status, byte[] bodyBytes) {
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public String body() {
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }
}
