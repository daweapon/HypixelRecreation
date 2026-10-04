package net.swofty.service.control.fleet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Everything the dashboard renders in one poll. */
public record FleetSnapshot(
        Instant takenAt,
        boolean dockerReachable,
        String dockerError,
        List<DependencyStatus> dependencies,
        List<ContainerInfo> containers,
        int integrity,
        Diagnosis diagnosis
) {
    /** Weighting behind the integrity figure at the centre of the reactor ring. A shared
     *  dependency being down matters far more than one idle world server being stopped. */
    private static final int DEPENDENCY_WEIGHT = 10;
    private static final int GAME_SERVER_WEIGHT = 2;
    private static final int SERVICE_WEIGHT = 2;
    private static final int OTHER_WEIGHT = 1;

    public static FleetSnapshot unreachable(String error) {
        List<DependencyStatus> dependencies = new ArrayList<>();
        for (SharedDependency dependency : SharedDependency.values()) {
            dependencies.add(DependencyStatus.absent(dependency));
        }
        return new FleetSnapshot(
                Instant.now(), false, error, dependencies, List.of(), 0,
                new Diagnosis(Diagnosis.Severity.CRITICAL, "Control plane cannot reach Docker",
                        error + " Fleet state is unknown - this is a fault in the control agent "
                                + "itself, not necessarily in the stack.")
        );
    }

    public static FleetSnapshot of(List<ContainerInfo> containers) {
        List<DependencyStatus> dependencies = resolveDependencies(containers);
        Diagnosis diagnosis = Diagnosis.evaluate(dependencies, containers);
        return new FleetSnapshot(Instant.now(), true, null, dependencies, containers,
                computeIntegrity(dependencies, containers), diagnosis);
    }

    private static List<DependencyStatus> resolveDependencies(List<ContainerInfo> containers) {
        List<DependencyStatus> dependencies = new ArrayList<>();
        for (SharedDependency dependency : SharedDependency.values()) {
            ContainerInfo match = containers.stream()
                    .filter(container -> dependency.matches(container.name()))
                    .findFirst()
                    .orElse(null);
            dependencies.add(match == null
                    ? DependencyStatus.absent(dependency)
                    : DependencyStatus.from(dependency, match));
        }
        return dependencies;
    }

    private static int computeIntegrity(List<DependencyStatus> dependencies, List<ContainerInfo> containers) {
        int earned = 0;
        int total = 0;

        for (DependencyStatus dependency : dependencies) {
            total += DEPENDENCY_WEIGHT;
            if (dependency.isNominal()) {
                earned += DEPENDENCY_WEIGHT;
            }
        }

        for (ContainerInfo container : containers) {
            if (SharedDependency.of(container.name()) != null
                    || FleetGroup.isOneShot(container.name())) {
                continue;
            }
            int weight = switch (container.group()) {
                case GAME_SERVER -> GAME_SERVER_WEIGHT;
                case SERVICE -> SERVICE_WEIGHT;
                default -> OTHER_WEIGHT;
            };
            total += weight;
            if (container.state().isNominal()) {
                earned += weight;
            } else if (container.state() == ContainerState.WRN) {
                earned += weight / 2;
            }
        }

        if (total == 0) {
            return 0;
        }
        return Math.round((earned * 100f) / total);
    }

    public Map<FleetGroup, List<ContainerInfo>> groupedContainers() {
        return containers.stream().collect(Collectors.groupingBy(ContainerInfo::group));
    }
}
