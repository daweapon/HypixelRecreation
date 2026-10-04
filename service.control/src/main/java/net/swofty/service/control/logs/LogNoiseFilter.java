package net.swofty.service.control.logs;

import java.util.List;
import java.util.Locale;

/**
 * Suppresses the boot-race noise that every container in this stack emits on startup.
 *
 * These three patterns are known-harmless and have repeatedly buried real errors during deploy
 * verification, which is why the log view ships filtered by default with RAW a toggle away
 * rather than the other way round.
 */
public final class LogNoiseFilter {

    public enum Mode {
        FILTERED,
        RAW;

        public static Mode parse(String value) {
            if (value == null) {
                return FILTERED;
            }
            return value.equalsIgnoreCase("raw") ? RAW : FILTERED;
        }
    }

    private static final List<String> NOISE_PATTERNS = List.of(
            "not online",
            "jedisconnectionexception",
            "socket is closed"
    );

    private LogNoiseFilter() {
    }

    public static boolean isNoise(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        String lower = line.toLowerCase(Locale.ROOT);
        return NOISE_PATTERNS.stream().anyMatch(lower::contains);
    }

    public static List<String> patterns() {
        return NOISE_PATTERNS;
    }
}
