package net.swofty.service.control;

import net.swofty.service.control.deploy.DeployTargets;
import net.swofty.service.control.docker.TarWriter;
import net.swofty.service.control.fleet.ContainerInfo;
import net.swofty.service.control.fleet.ContainerState;
import net.swofty.service.control.fleet.FleetGroup;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeployTest {

    private static ContainerInfo container(String name) {
        return new ContainerInfo("id", name, "image", FleetGroup.classify(name),
                ContainerState.UP, "running", "none", 0, 0, Instant.now(), "running");
    }

    private static final List<ContainerInfo> FLEET = List.of(
            container("hypixel_mongo"),
            container("service_api"),
            container("service_party"),
            container("service_control"),
            container("hypixelcore_skyblock_hub"),
            container("hypixelcore_skyblock_island"),
            container("hypixelcore_prototype_lobby"));

    // --- tar ---

    @Test
    void tarIsBlockAlignedAndTerminated() {
        byte[] tar = TarWriter.singleFile("X.jar", "hello".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, tar.length % 512, "a tar stream must be a whole number of 512b blocks");
        // header + one data block + two terminating blocks
        assertEquals(512 * 4, tar.length);
        for (int i = tar.length - 1024; i < tar.length; i++) {
            assertEquals(0, tar[i], "archive must end with two zero blocks");
        }
    }

    @Test
    void tarHeaderCarriesNameSizeAndValidChecksum() {
        byte[] content = new byte[1000];
        byte[] tar = TarWriter.singleFile("ServiceAPI.jar", content);

        String name = new String(tar, 0, 14, StandardCharsets.UTF_8);
        assertEquals("ServiceAPI.jar", name);

        String size = new String(tar, 124, 11, StandardCharsets.UTF_8).trim();
        assertEquals(1000, Long.parseLong(size, 8), "size field must be octal and correct");

        assertEquals("ustar", new String(tar, 257, 5, StandardCharsets.UTF_8));

        long stored = Long.parseLong(new String(tar, 148, 7, StandardCharsets.UTF_8).trim(), 8);
        long computed = 0;
        for (int i = 0; i < 512; i++) {
            computed += (i >= 148 && i < 156) ? ' ' : (tar[i] & 0xFF);
        }
        assertEquals(computed, stored, "checksum must validate, or Docker rejects the archive");
    }

    @Test
    void payloadSurvivesExactly() {
        byte[] content = "jar bytes here".getBytes(StandardCharsets.UTF_8);
        byte[] tar = TarWriter.singleFile("A.jar", content);
        byte[] extracted = new byte[content.length];
        System.arraycopy(tar, 512, extracted, 0, content.length);
        assertEquals(new String(content, StandardCharsets.UTF_8),
                new String(extracted, StandardCharsets.UTF_8));
    }

    // --- targets ---

    @Test
    void theCoreJarGoesToEveryGameServerNotJustOne() {
        // A partial rollout of HypixelCore.jar is the version-skew bug (tracker #85), where
        // mismatched jars looked like a player's inventory had been wiped.
        List<String> targets = DeployTargets.defaultTargets("HypixelCore.jar", FLEET);
        assertEquals(3, targets.size());
        assertTrue(targets.contains("hypixelcore_skyblock_hub"));
        assertTrue(targets.contains("hypixelcore_skyblock_island"));
        assertTrue(targets.contains("hypixelcore_prototype_lobby"));
    }

    @Test
    void serviceJarsResolveToTheirOwnContainerOnly() {
        assertEquals(List.of("service_api"), DeployTargets.defaultTargets("ServiceAPI.jar", FLEET));
        assertEquals(List.of("service_party"), DeployTargets.defaultTargets("ServiceParty.jar", FLEET));
    }

    @Test
    void aServiceNotPresentInTheFleetResolvesToNothing() {
        assertTrue(DeployTargets.defaultTargets("ServiceGuild.jar", FLEET).isEmpty(),
                "deploying to a container that is not here must not silently pick a wrong one");
    }

    @Test
    void unknownJarsResolveToNothingRatherThanGuessing() {
        assertTrue(DeployTargets.defaultTargets("Mystery.jar", FLEET).isEmpty());
        assertTrue(DeployTargets.defaultTargets("ServiceNonexistent.jar", FLEET).isEmpty());
    }

    @Test
    void onlyTheControlJarIsTreatedAsBindMounted() {
        assertTrue(DeployTargets.isBindMounted("ServiceControl.jar"));
        assertFalse(DeployTargets.isBindMounted("ServiceAPI.jar"));
        assertFalse(DeployTargets.isBindMounted("HypixelCore.jar"));
    }

    @Test
    void theAgentRefusesToDeployItsOwnJar() {
        assertNotNull(DeployTargets.selfDeployRefusal("ServiceControl.jar", List.of()));
        assertNotNull(DeployTargets.selfDeployRefusal("servicecontrol.jar", null));
    }

    @Test
    void theAgentRefusesAnyJarExplicitlyTargetedAtItsOwnContainer() {
        assertNotNull(DeployTargets.selfDeployRefusal("ServiceAPI.jar", List.of("service_api", "service_control")));
    }

    @Test
    void ordinaryDeploysAreNotRefused() {
        assertNull(DeployTargets.selfDeployRefusal("HypixelCore.jar", List.of()));
        assertNull(DeployTargets.selfDeployRefusal("ServiceAPI.jar", List.of("service_api")));
    }
}
