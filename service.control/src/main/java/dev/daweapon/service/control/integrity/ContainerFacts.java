package dev.daweapon.service.control.integrity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The inspect fields the integrity checks need, parsed once from Docker's inspect document.
 *
 * @param memoryLimitBytes HostConfig.Memory; 0 means the container has no limit
 * @param heapBytes        the JVM -Xmx found in SERVICE_CMD, JAVA_TOOL_OPTIONS or JAVA_OPTS; 0 if none
 */
public record ContainerFacts(
        String name,
        String id,
        String hostname,
        boolean running,
        Instant startedAt,
        ServiceCommand command,
        long memoryLimitBytes,
        long heapBytes,
        String ipAddress
) {
    public static ContainerFacts from(String fallbackName, JsonObject inspect) {
        JsonObject config = object(inspect, "Config");
        JsonObject state = object(inspect, "State");
        JsonObject hostConfig = object(inspect, "HostConfig");

        String name = string(inspect, "Name", fallbackName);
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        List<String> env = new ArrayList<>();
        if (config != null && config.has("Env") && config.get("Env").isJsonArray()) {
            for (JsonElement element : config.getAsJsonArray("Env")) {
                env.add(element.getAsString());
            }
        }

        long heap = 0;
        for (String variable : env) {
            if (variable.startsWith("SERVICE_CMD=") || variable.startsWith("JAVA_TOOL_OPTIONS=")
                    || variable.startsWith("JAVA_OPTS=")) {
                heap = Math.max(heap, parseXmx(variable.substring(variable.indexOf('=') + 1)));
            }
        }

        return new ContainerFacts(
                name,
                string(inspect, "Id", ""),
                string(config, "Hostname", ""),
                state != null && state.has("Running") && state.get("Running").getAsBoolean(),
                parseInstant(string(state, "StartedAt", null)),
                ServiceCommand.fromEnv(env),
                hostConfig != null && hostConfig.has("Memory") && !hostConfig.get("Memory").isJsonNull()
                        ? hostConfig.get("Memory").getAsLong() : 0L,
                heap,
                firstIpAddress(inspect)
        );
    }

    /** True when a registration's host names this container: its hostname, full id or short id. */
    public boolean answersTo(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        return host.equals(hostname) || host.equals(name) || (!id.isEmpty() && id.startsWith(host));
    }

    public static long parseXmx(String text) {
        if (text == null) {
            return 0;
        }
        for (String token : text.trim().split("\\s+")) {
            if (!token.startsWith("-Xmx") || token.length() < 5) {
                continue;
            }
            String value = token.substring(4).toLowerCase(Locale.ROOT);
            long multiplier = switch (value.charAt(value.length() - 1)) {
                case 'k' -> 1024L;
                case 'm' -> 1024L * 1024;
                case 'g' -> 1024L * 1024 * 1024;
                default -> 1L;
            };
            String digits = Character.isDigit(value.charAt(value.length() - 1))
                    ? value : value.substring(0, value.length() - 1);
            try {
                return Long.parseLong(digits) * multiplier;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static String firstIpAddress(JsonObject inspect) {
        JsonObject settings = object(inspect, "NetworkSettings");
        JsonObject networks = object(settings, "Networks");
        if (networks == null) {
            return null;
        }
        for (String network : networks.keySet()) {
            JsonElement element = networks.get(network);
            if (element.isJsonObject()) {
                String ip = string(element.getAsJsonObject(), "IPAddress", "");
                if (!ip.isBlank()) {
                    return ip;
                }
            }
        }
        return null;
    }

    private static JsonObject object(JsonObject parent, String key) {
        if (parent == null || !parent.has(key) || !parent.get(key).isJsonObject()) {
            return null;
        }
        return parent.getAsJsonObject(key);
    }

    private static String string(JsonObject parent, String key, String fallback) {
        if (parent == null || !parent.has(key) || parent.get(key).isJsonNull()) {
            return fallback;
        }
        return parent.get(key).getAsString();
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isEmpty() || value.startsWith("0001-01-01")) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
