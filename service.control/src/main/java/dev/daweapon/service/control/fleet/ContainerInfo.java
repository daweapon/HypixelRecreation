package dev.daweapon.service.control.fleet;

import java.time.Duration;
import java.time.Instant;

/**
 * One container as the control plane sees it.
 *
 * @param dockerState raw Docker state string ("running", "exited", ...), kept for diagnosis
 * @param health      raw healthcheck status, or "none" when the container declares no healthcheck
 * @param exitCode    last exit code; only meaningful once the container has stopped
 */
public record ContainerInfo(
        String id,
        String name,
        String image,
        FleetGroup group,
        ContainerState state,
        String dockerState,
        String health,
        int restartCount,
        int exitCode,
        Instant startedAt,
        String statusText
) {
    public Duration uptime() {
        if (startedAt == null || state != ContainerState.UP) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, Instant.now());
    }

    /**
     * Derives the short status code from Docker's raw state and healthcheck.
     *
     * Restart count is deliberately NOT consulted here. It is cumulative over the container's
     * whole life, so a container carrying restarts from an incident days ago would read as a
     * loop the moment it was restarted by hand. Actively looping is detected instead by the
     * watchdog, from the restart count *rising* between polls. Here only Docker's own
     * "restarting" state counts.
     */
    public static ContainerState deriveState(String dockerState, String health, int restartCount, Instant startedAt) {
        if (dockerState == null) {
            return ContainerState.DWN;
        }
        return switch (dockerState.toLowerCase()) {
            case "running" -> deriveRunningState(health, restartCount, startedAt);
            case "restarting" -> ContainerState.LOOP;
            case "exited" -> ContainerState.EXIT;
            default -> ContainerState.DWN;
        };
    }

    private static ContainerState deriveRunningState(String health, int restartCount, Instant startedAt) {
        if (health == null || health.isEmpty() || health.equals("none")) {
            return ContainerState.UP;
        }
        return switch (health.toLowerCase()) {
            case "healthy" -> ContainerState.UP;
            case "starting", "unhealthy" -> ContainerState.WRN;
            default -> ContainerState.UP;
        };
    }
}
