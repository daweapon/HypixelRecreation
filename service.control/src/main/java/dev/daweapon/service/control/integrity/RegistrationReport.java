package dev.daweapon.service.control.integrity;

import dev.daweapon.service.control.watchdog.Alert;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The proxy-registration verdict, as last pushed by service.api.
 *
 * The agent has no Redis by design, so it cannot ask the proxy itself; service.api does and
 * posts the DELISTED set here, where the watchdog turns it into alerts with the same debounce
 * and recovery notices as every other condition. A report that stops arriving is dropped
 * rather than trusted, so a dead API cannot keep a stale alert firing.
 */
public final class RegistrationReport implements Supplier<Map<String, Alert>> {
    private static final Duration MAX_AGE = Duration.ofMinutes(3);

    private record Snapshot(Instant receivedAt, boolean proxyReachable, List<String> delisted) {
    }

    private volatile Snapshot latest;

    public void update(boolean proxyReachable, List<String> delisted) {
        latest = new Snapshot(Instant.now(), proxyReachable, List.copyOf(delisted));
    }

    @Override
    public Map<String, Alert> get() {
        return conditions(latest == null ? null : latest.receivedAt(),
                latest != null && latest.proxyReachable(),
                latest == null ? List.of() : latest.delisted(), Instant.now());
    }

    public static Map<String, Alert> conditions(Instant receivedAt, boolean proxyReachable,
                                                List<String> delisted, Instant now) {
        Map<String, Alert> conditions = new LinkedHashMap<>();
        if (receivedAt == null || Duration.between(receivedAt, now).compareTo(MAX_AGE) > 0 || !proxyReachable) {
            return conditions;
        }
        for (String container : delisted) {
            String key = "delisted:" + container;
            conditions.put(key, Alert.warning(key, container + " is not registered with the proxy",
                    "The container is running but the proxy has no server for it, so players are "
                            + "told there are no servers available. The proxy drops a server after 5 "
                            + "missed pings and nothing re-registers it; restart " + container
                            + " (Re-register in the app) to bring it back."));
        }
        return conditions;
    }
}
