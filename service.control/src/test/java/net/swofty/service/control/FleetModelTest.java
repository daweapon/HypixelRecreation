package net.swofty.service.control;

import net.swofty.service.control.fleet.ContainerInfo;
import net.swofty.service.control.fleet.ContainerState;
import net.swofty.service.control.fleet.Diagnosis;
import net.swofty.service.control.fleet.FleetGroup;
import net.swofty.service.control.fleet.FleetSnapshot;
import net.swofty.service.control.fleet.SharedDependency;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetModelTest {

    private static ContainerInfo container(String name, ContainerState state) {
        return new ContainerInfo("id-" + name, name, "image", FleetGroup.classify(name), state,
                state == ContainerState.UP ? "running" : "exited", "none", 0, 0,
                Instant.now().minusSeconds(3600), "status");
    }

    @Test
    void classifiesContainersByInstallerNamingConvention() {
        assertEquals(FleetGroup.CORE, FleetGroup.classify("hypixel_mongo"));
        assertEquals(FleetGroup.CORE, FleetGroup.classify("pico_limbo"));
        assertEquals(FleetGroup.SERVICE, FleetGroup.classify("service_party"));
        assertEquals(FleetGroup.GAME_SERVER, FleetGroup.classify("hypixelcore_skyblock_hub"));
        assertEquals(FleetGroup.GAME_SERVER, FleetGroup.classify("prototype_lobby"));
        assertEquals(FleetGroup.UNKNOWN, FleetGroup.classify("some_unrelated_container"));
    }

    @Test
    void matchesSharedDependenciesAcrossBothNamingSchemes() {
        assertEquals(SharedDependency.MONGO, SharedDependency.of("hypixel_mongo"));
        assertEquals(SharedDependency.MONGO, SharedDependency.of("mongodb"));
        assertEquals(SharedDependency.REDIS, SharedDependency.of("redis"));
        assertEquals(SharedDependency.PROXY, SharedDependency.of("hypixel_proxy"));
    }

    @Test
    void derivesRunningWithoutHealthcheckAsUp() {
        assertEquals(ContainerState.UP,
                ContainerInfo.deriveState("running", "none", 0, Instant.now().minusSeconds(600)));
    }

    @Test
    void derivesUnhealthyAsWarningNotDown() {
        assertEquals(ContainerState.WRN,
                ContainerInfo.deriveState("running", "unhealthy", 0, Instant.now().minusSeconds(600)));
        assertEquals(ContainerState.WRN,
                ContainerInfo.deriveState("running", "starting", 0, Instant.now().minusSeconds(5)));
    }

    @Test
    void derivesRestartingAsLoop() {
        assertEquals(ContainerState.LOOP,
                ContainerInfo.deriveState("restarting", "none", 9, Instant.now()));
    }

    @Test
    void doesNotCallAnOldContainerWithHistoricRestartsALoop() {
        // Restarts from an incident days ago must not read as an active loop.
        assertEquals(ContainerState.UP,
                ContainerInfo.deriveState("running", "none", 40, Instant.now().minusSeconds(86400)));
    }

    @Test
    void diagnosisLeadsWithSharedDependencyFailure() {
        List<ContainerInfo> containers = List.of(
                container("hypixel_mongo", ContainerState.EXIT),
                container("hypixel_redis", ContainerState.EXIT),
                container("hypixel_proxy", ContainerState.EXIT),
                container("service_party", ContainerState.UP),
                container("hypixelcore_skyblock_hub", ContainerState.EXIT));

        FleetSnapshot snapshot = FleetSnapshot.of(containers);
        assertEquals(Diagnosis.Severity.CRITICAL, snapshot.diagnosis().severity());
        assertEquals("Mongo, Redis and Proxy down", snapshot.diagnosis().headline());
        assertTrue(snapshot.diagnosis().detail().contains("retry loop"));
    }

    @Test
    void diagnosisReportsNominalWhenEverythingIsUp() {
        List<ContainerInfo> containers = List.of(
                container("hypixel_mongo", ContainerState.UP),
                container("hypixel_redis", ContainerState.UP),
                container("hypixel_proxy", ContainerState.UP),
                container("service_party", ContainerState.UP));

        FleetSnapshot snapshot = FleetSnapshot.of(containers);
        assertEquals(Diagnosis.Severity.NOMINAL, snapshot.diagnosis().severity());
        assertEquals(100, snapshot.integrity());
    }

    @Test
    void integrityWeightsSharedDependenciesFarAboveASingleWorld() {
        List<ContainerInfo> oneWorldDown = List.of(
                container("hypixel_mongo", ContainerState.UP),
                container("hypixel_redis", ContainerState.UP),
                container("hypixel_proxy", ContainerState.UP),
                container("hypixelcore_skyblock_the_end", ContainerState.EXIT));

        List<ContainerInfo> mongoDown = List.of(
                container("hypixel_mongo", ContainerState.EXIT),
                container("hypixel_redis", ContainerState.UP),
                container("hypixel_proxy", ContainerState.UP),
                container("hypixelcore_skyblock_the_end", ContainerState.UP));

        assertTrue(FleetSnapshot.of(oneWorldDown).integrity() > FleetSnapshot.of(mongoDown).integrity(),
                "a stopped optional world must dent integrity far less than Mongo being down");
    }

    @Test
    void missingDependencyContainerIsReportedAsAbsentRatherThanHealthy() {
        FleetSnapshot snapshot = FleetSnapshot.of(List.of(container("service_party", ContainerState.UP)));
        assertEquals(3, snapshot.dependencies().size());
        assertTrue(snapshot.dependencies().stream().noneMatch(d -> d.isNominal()));
        assertFalse(snapshot.dependencies().get(0).present());
    }

    @Test
    void unreachableDockerIsItsOwnDiagnosisNotAHealthyStack() {
        FleetSnapshot snapshot = FleetSnapshot.unreachable("socket missing");
        assertFalse(snapshot.dockerReachable());
        assertEquals(0, snapshot.integrity());
        assertEquals(Diagnosis.Severity.CRITICAL, snapshot.diagnosis().severity());
        assertNotNull(snapshot.diagnosis().detail());
    }
}
