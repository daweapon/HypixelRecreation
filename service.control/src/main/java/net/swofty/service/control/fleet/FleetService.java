package net.swofty.service.control.fleet;

import net.swofty.service.control.docker.DockerApi;
import net.swofty.service.control.docker.DockerException;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Produces fleet snapshots, with a short cache.
 *
 * The dashboard polls frequently and every snapshot costs one list plus an inspect per
 * container, so repeated callers within the cache window share one result.
 */
public final class FleetService {
    private static final Duration CACHE_TTL = Duration.ofSeconds(2);

    private final DockerApi docker;
    private final AtomicReference<FleetSnapshot> cached = new AtomicReference<>();

    public FleetService(DockerApi docker) {
        this.docker = docker;
    }

    public FleetSnapshot snapshot() {
        FleetSnapshot current = cached.get();
        if (current != null && Duration.between(current.takenAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return current;
        }
        FleetSnapshot fresh = build();
        cached.set(fresh);
        return fresh;
    }

    /** Forces a fresh read, used right after a lifecycle action changes the world. */
    public FleetSnapshot refresh() {
        FleetSnapshot fresh = build();
        cached.set(fresh);
        return fresh;
    }

    private FleetSnapshot build() {
        if (!docker.isAvailable()) {
            return FleetSnapshot.unreachable("The Docker socket is not mounted into this container.");
        }
        try {
            return FleetSnapshot.of(docker.listProjectContainers());
        } catch (DockerException e) {
            Logger.error(e, "Failed to build fleet snapshot");
            return FleetSnapshot.unreachable(e.getMessage());
        }
    }
}
