package net.swofty.service.control.fleet;

/**
 * Grouping used by the fleet table, derived from the installer's own container naming
 * convention (setup/internal/installer/docker.go isProjectContainer).
 */
public enum FleetGroup {
    CORE,
    GAME_SERVER,
    SERVICE,
    UNKNOWN;

    private static final String SHARED_DEPENDENCY_PREFIX = "hypixel_";

    public static FleetGroup classify(String containerName) {
        if (containerName == null) {
            return UNKNOWN;
        }
        if (containerName.startsWith("service_")) {
            return SERVICE;
        }
        if (containerName.startsWith("hypixelcore_") || containerName.equals("prototype_lobby")) {
            return GAME_SERVER;
        }
        if (containerName.startsWith(SHARED_DEPENDENCY_PREFIX)
                || containerName.equals("pico_limbo")
                || containerName.equals("game_server_builder")) {
            return CORE;
        }
        return UNKNOWN;
    }

    public static boolean isProjectContainer(String containerName) {
        return classify(containerName) != UNKNOWN;
    }
}
