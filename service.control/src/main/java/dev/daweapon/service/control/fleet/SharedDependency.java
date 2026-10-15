package dev.daweapon.service.control.fleet;

import java.util.List;

/**
 * The three shared dependencies drawn as arcs of the reactor ring.
 *
 * These are singled out from the rest of the fleet because when any of them is down, every game
 * server retry-loops and CPU goes uniformly high - a pattern that has twice been misread as a
 * per-server performance bug. Leading with these three is the whole point of the dashboard.
 *
 * Each carries several candidate container names: compose service names and container_name values
 * have diverged across installs (hypixel_mongo vs mongodb), so matching is by candidate list
 * rather than a single hardcoded string.
 */
public enum SharedDependency {
    MONGO("Mongo", List.of("hypixel_mongo", "mongodb", "mongo")),
    REDIS("Redis", List.of("hypixel_redis", "redis")),
    PROXY("Proxy", List.of("hypixel_proxy", "proxy"));

    private final String displayName;
    private final List<String> candidateNames;

    SharedDependency(String displayName, List<String> candidateNames) {
        this.displayName = displayName;
        this.candidateNames = candidateNames;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean matches(String containerName) {
        return containerName != null && candidateNames.contains(containerName.toLowerCase());
    }

    public static SharedDependency of(String containerName) {
        for (SharedDependency dependency : values()) {
            if (dependency.matches(containerName)) {
                return dependency;
            }
        }
        return null;
    }
}
