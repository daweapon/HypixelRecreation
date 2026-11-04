package dev.daweapon.service.api.control;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Proxy registration state per game server (capability 3), kept apart from container state.
 *
 * A container can be up while the proxy has dropped it: the proxy removes a server after five
 * missed pings and nothing re-registers it, so players are told no servers are available while
 * every container reads UP. Registrations are matched to containers by the hostname the server
 * registered with (its container's hostname), falling back to server type for a proxy that does
 * not report hosts.
 */
public final class ProxyRegistrations {
    /** A server needs time to boot and register; until then missing is not yet DELISTED. */
    public static final Duration BOOT_GRACE = Duration.ofMinutes(3);

    private static final Set<String> RUNNING_STATES = Set.of("UP", "WRN");

    public enum State {
        REGISTERED,
        DELISTED,
        BOOTING,
        NOT_RUNNING,
        UNKNOWN
    }

    public record Registration(String name, String uuid, String type, int port, int players,
                               @Nullable String host) {
    }

    public record Server(String container, String containerState, @Nullable Instant startedAt,
                         String id, @Nullable String hostname, @Nullable String serverType) {
        boolean running() {
            return RUNNING_STATES.contains(containerState);
        }

        boolean answersTo(@Nullable String host) {
            if (host == null || host.isBlank()) {
                return false;
            }
            return host.equals(hostname) || host.equals(container) || (!id.isEmpty() && id.startsWith(host));
        }
    }

    public record Row(String container, @Nullable String serverType, String containerState, State state,
                      @Nullable String proxyName, int players, String detail) {
    }

    public record Result(boolean proxyReachable, @Nullable String proxyError, String severity,
                         String headline, String detail, List<Row> rows, List<Registration> unmatched,
                         List<String> delisted) {
    }

    private ProxyRegistrations() {
    }

    /** @param registrations the proxy's list, or null when the proxy could not be asked */
    public static Result match(List<Server> servers, @Nullable List<Registration> registrations,
                               @Nullable String proxyError, Instant now) {
        List<Registration> pool = registrations == null ? new ArrayList<>() : new ArrayList<>(registrations);
        List<Row> rows = new ArrayList<>();
        List<Server> unmatched = new ArrayList<>();

        for (Server server : servers) {
            if (!server.running()) {
                rows.add(row(server, State.NOT_RUNNING, null, "container is not running"));
            } else if (registrations == null) {
                rows.add(row(server, State.UNKNOWN, null, "proxy could not be asked"));
            } else {
                Registration byHost = take(pool, candidate -> server.answersTo(candidate.host()));
                if (byHost != null) {
                    rows.add(row(server, State.REGISTERED, byHost, "registered as " + byHost.name()));
                } else {
                    unmatched.add(server);
                }
            }
        }

        List<String> delisted = new ArrayList<>();
        for (Server server : unmatched) {
            Registration byType = take(pool, candidate -> candidate.type().equals(server.serverType())
                    && servers.stream().noneMatch(other -> other.answersTo(candidate.host())));
            if (byType != null) {
                rows.add(row(server, State.REGISTERED, byType, "registered as " + byType.name() + " (matched by type)"));
            } else if (server.startedAt() != null && Duration.between(server.startedAt(), now).compareTo(BOOT_GRACE) < 0) {
                rows.add(row(server, State.BOOTING, null, "started recently; not registered yet"));
            } else {
                rows.add(row(server, State.DELISTED, null, "running, but the proxy has no server for it"));
                delisted.add(server.container());
            }
        }

        rows.sort(java.util.Comparator.comparing(Row::container));
        return summarise(registrations != null, proxyError, rows, pool, delisted);
    }

    private static Result summarise(boolean reachable, @Nullable String proxyError, List<Row> rows,
                                    List<Registration> unmatched, List<String> delisted) {
        if (!reachable) {
            return new Result(false, proxyError, "WARNING", "Proxy registrations unknown",
                    "The proxy did not answer over Redis" + (proxyError == null ? "" : " (" + proxyError + ")")
                            + ". Container state is still accurate; registration state is not known.",
                    rows, unmatched, delisted);
        }
        if (!delisted.isEmpty()) {
            return new Result(true, null, "CRITICAL",
                    delisted.size() + " game server(s) running but not registered with the proxy",
                    "Players are told there are no servers available for " + String.join(", ", delisted)
                            + " although the container is up. The proxy drops a server after 5 missed pings "
                            + "and nothing re-registers it; Re-register restarts the server so it registers again.",
                    rows, unmatched, delisted);
        }
        long registered = rows.stream().filter(row -> row.state() == State.REGISTERED).count();
        return new Result(true, null, "NOMINAL", registered + " game server(s) registered with the proxy",
                unmatched.isEmpty() ? "Every running game server is registered."
                        : unmatched.size() + " proxy registration(s) match no container.",
                rows, unmatched, delisted);
    }

    private static Row row(Server server, State state, @Nullable Registration registration, String detail) {
        return new Row(server.container(), server.serverType(), server.containerState(), state,
                registration == null ? null : registration.name(),
                registration == null ? 0 : registration.players(), detail);
    }

    private static @Nullable Registration take(List<Registration> pool,
                                              java.util.function.Predicate<Registration> test) {
        Iterator<Registration> iterator = pool.iterator();
        while (iterator.hasNext()) {
            Registration candidate = iterator.next();
            if (test.test(candidate)) {
                iterator.remove();
                return candidate;
            }
        }
        return null;
    }
}
