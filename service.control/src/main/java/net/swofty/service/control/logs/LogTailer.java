package net.swofty.service.control.logs;

import net.swofty.service.control.docker.DockerApi;

import java.util.ArrayList;
import java.util.List;

/** Fetches and post-processes a container's recent log output. */
public final class LogTailer {
    private static final int MAX_TAIL = 2000;

    private final DockerApi docker;

    public LogTailer(DockerApi docker) {
        this.docker = docker;
    }

    public record LogLine(String timestamp, String message, boolean noise) {
    }

    public record LogResult(String container, LogNoiseFilter.Mode mode, int totalLines,
                            int suppressedLines, List<LogLine> lines) {
    }

    public LogResult tail(String container, int requestedTail, LogNoiseFilter.Mode mode) {
        int tail = Math.min(Math.max(requestedTail, 1), MAX_TAIL);
        String raw = docker.logs(container, tail);

        List<LogLine> lines = new ArrayList<>();
        int suppressed = 0;
        int total = 0;

        for (String line : raw.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            total++;
            boolean noise = LogNoiseFilter.isNoise(line);
            if (noise && mode == LogNoiseFilter.Mode.FILTERED) {
                suppressed++;
                continue;
            }
            lines.add(split(line, noise));
        }

        return new LogResult(container, mode, total, suppressed, lines);
    }

    /**
     * Separates the RFC3339 timestamp Docker prefixes when timestamps=true, so the client can
     * render it in a dimmer column instead of inline with the message.
     */
    private LogLine split(String line, boolean noise) {
        int firstSpace = line.indexOf(' ');
        if (firstSpace > 0 && firstSpace < 40 && line.length() > 5 && line.charAt(4) == '-') {
            return new LogLine(line.substring(0, firstSpace), line.substring(firstSpace + 1), noise);
        }
        return new LogLine(null, line, noise);
    }
}
