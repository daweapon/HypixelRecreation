package net.swofty.service.control.docker;

// Raised when the Docker Engine API is unreachable or answers with a non-success status.
public class DockerException extends RuntimeException {
    public DockerException(String message) {
        super(message);
    }

    public DockerException(String message, Throwable cause) {
        super(message, cause);
    }
}
