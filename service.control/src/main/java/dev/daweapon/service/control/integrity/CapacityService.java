package dev.daweapon.service.control.integrity;

import com.google.gson.JsonObject;
import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetService;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads the numbers behind {@link CapacityBudget} from Docker.
 *
 * Host RAM comes from the daemon's /info (MemTotal), so the figure is the NAS's real memory
 * rather than a constant; the fallback is the RS2423+'s 32 GB.
 */
public final class CapacityService {
    private static final long MIB = 1024L * 1024;
    private static final long FALLBACK_HOST_BYTES = 32L * 1024 * MIB;

    private final DockerApi docker;
    private final FleetService fleetService;
    private final long reserveBytes;
    private final long flatEstimateBytes;

    public CapacityService(DockerApi docker, FleetService fleetService) {
        this.docker = docker;
        this.fleetService = fleetService;
        this.reserveBytes = readMib("CAPACITY_HOST_RESERVE_MB", 4096) * MIB;
        this.flatEstimateBytes = readMib("CAPACITY_UNLIMITED_ESTIMATE_MB", 1024) * MIB;
    }

    public CapacityBudget.Budget evaluate(Set<String> candidates) {
        long hostBytes = FALLBACK_HOST_BYTES;
        try {
            JsonObject info = docker.info();
            if (info.has("MemTotal")) {
                hostBytes = info.get("MemTotal").getAsLong();
            }
        } catch (DockerException e) {
            Logger.warn("Docker /info unavailable, assuming {} bytes of host RAM: {}", hostBytes, e.getMessage());
        }

        List<CapacityBudget.Container> containers = new ArrayList<>();
        for (ContainerInfo container : fleetService.snapshot().containers()) {
            if (container.group() == FleetGroup.UNKNOWN || FleetGroup.isOneShot(container.name())) {
                continue;
            }
            try {
                JsonObject inspect = docker.inspectRaw(container.name());
                if (inspect == null) {
                    continue;
                }
                ContainerFacts facts = ContainerFacts.from(container.name(), inspect);
                containers.add(new CapacityBudget.Container(container.name(), facts.running(),
                        facts.memoryLimitBytes(), facts.heapBytes()));
            } catch (DockerException e) {
                Logger.warn("Capacity check could not inspect {}: {}", container.name(), e.getMessage());
            }
        }
        return CapacityBudget.evaluate(hostBytes, reserveBytes, flatEstimateBytes, containers, candidates);
    }

    private static long readMib(String name, long fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
