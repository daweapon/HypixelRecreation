package dev.daweapon.service.control.fleet;

/** One arc of the reactor ring: a shared dependency and the container backing it. */
public record DependencyStatus(
        SharedDependency dependency,
        String containerName,
        ContainerState state,
        String statusText,
        boolean present
) {
    public static DependencyStatus absent(SharedDependency dependency) {
        return new DependencyStatus(dependency, null, ContainerState.DWN,
                "container not found", false);
    }

    public static DependencyStatus from(SharedDependency dependency, ContainerInfo container) {
        return new DependencyStatus(dependency, container.name(), container.state(),
                container.statusText(), true);
    }

    public boolean isNominal() {
        return present && state.isNominal();
    }
}
