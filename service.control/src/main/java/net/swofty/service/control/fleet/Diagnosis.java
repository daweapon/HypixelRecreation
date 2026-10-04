package net.swofty.service.control.fleet;

import java.util.List;

/**
 * A plain-language reading of what is wrong, and what to do about it.
 *
 * Every branch below encodes a failure mode this project has actually hit, so the operator is
 * told what the pattern means rather than being left to infer it from a wall of container rows.
 */
public record Diagnosis(Severity severity, String headline, String detail) {

    public enum Severity {
        NOMINAL,
        WARNING,
        CRITICAL
    }

    public static Diagnosis evaluate(List<DependencyStatus> dependencies, List<ContainerInfo> containers) {
        List<DependencyStatus> brokenDependencies = dependencies.stream()
                .filter(dependency -> !dependency.isNominal())
                .toList();

        if (!brokenDependencies.isEmpty()) {
            return sharedDependencyFailure(brokenDependencies, containers);
        }

        List<ContainerInfo> looping = containers.stream()
                .filter(container -> container.state() == ContainerState.LOOP)
                .toList();
        if (!looping.isEmpty()) {
            return new Diagnosis(Severity.CRITICAL,
                    looping.size() + " container(s) restarting in a loop",
                    "The shared dependencies are up, so this is not the usual cause. "
                            + "Check the logs of " + names(looping) + " for a startup failure.");
        }

        List<ContainerInfo> stoppedGameServers = containers.stream()
                .filter(container -> container.group() == FleetGroup.GAME_SERVER)
                .filter(container -> container.state().isCritical())
                .toList();
        if (!stoppedGameServers.isEmpty()) {
            return new Diagnosis(Severity.WARNING,
                    stoppedGameServers.size() + " game server(s) not running",
                    "Players will see \"There are no servers available\" for "
                            + names(stoppedGameServers) + ".");
        }

        List<ContainerInfo> stoppedServices = containers.stream()
                .filter(container -> container.group() == FleetGroup.SERVICE)
                .filter(container -> container.state().isCritical())
                .toList();
        if (!stoppedServices.isEmpty()) {
            return new Diagnosis(Severity.WARNING,
                    stoppedServices.size() + " service(s) not running",
                    "Features backed by " + names(stoppedServices) + " will fail silently in game.");
        }

        List<ContainerInfo> unhealthy = containers.stream()
                .filter(container -> container.state() == ContainerState.WRN)
                .toList();
        if (!unhealthy.isEmpty()) {
            return new Diagnosis(Severity.WARNING,
                    unhealthy.size() + " container(s) not yet healthy",
                    "Still starting, or failing their healthcheck: " + names(unhealthy) + ".");
        }

        return new Diagnosis(Severity.NOMINAL, "Stack nominal", "All containers up and healthy.");
    }

    private static Diagnosis sharedDependencyFailure(List<DependencyStatus> broken, List<ContainerInfo> containers) {
        String brokenNames = joinNaturally(broken.stream()
                .map(dependency -> dependency.dependency().getDisplayName())
                .toList());

        long retryLooping = containers.stream()
                .filter(container -> container.group() != FleetGroup.CORE)
                .filter(container -> container.state() == ContainerState.UP
                        || container.state() == ContainerState.LOOP)
                .count();

        String detail = brokenNames + " "
                + (broken.size() > 1 ? "are" : "is") + " not available. "
                + "Until fixed, the " + retryLooping + " running server(s) and service(s) will sit in a "
                + "connection retry loop and drive CPU uniformly high across the whole stack - this is "
                + "not a per-server performance bug. Start the shared dependencies first, wait for them "
                + "to settle, then bring the game servers back.";

        return new Diagnosis(Severity.CRITICAL, brokenNames + " down", detail);
    }

    /** "Mongo", "Mongo and Redis", "Mongo, Redis and Proxy". */
    private static String joinNaturally(List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        String head = String.join(", ", values.subList(0, values.size() - 1));
        return head + " and " + values.get(values.size() - 1);
    }

    private static String names(List<ContainerInfo> containers) {
        return String.join(", ", containers.stream().map(ContainerInfo::name).toList());
    }
}
