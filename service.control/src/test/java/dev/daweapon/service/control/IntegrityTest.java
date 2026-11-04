package dev.daweapon.service.control;

import com.google.gson.JsonParser;
import dev.daweapon.service.control.docker.TarWriter;
import dev.daweapon.service.control.integrity.BootVerifier;
import dev.daweapon.service.control.integrity.CapacityBudget;
import dev.daweapon.service.control.integrity.ConfigDrift;
import dev.daweapon.service.control.integrity.ContainerFacts;
import dev.daweapon.service.control.integrity.HttpProbe;
import dev.daweapon.service.control.integrity.JarSkew;
import dev.daweapon.service.control.integrity.PackVerdict;
import dev.daweapon.service.control.integrity.RegistrationReport;
import dev.daweapon.service.control.integrity.ServiceCommand;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrityTest {
    private static final String A = "a".repeat(64);
    private static final String B = "b".repeat(64);
    private static final long GIB = 1024L * 1024 * 1024;

    // --- what a container runs ---

    @Test
    void serviceCommandReadsJarAndServerType() {
        ServiceCommand command = ServiceCommand.parse("java -Xmx3G -jar HypixelCore.jar SKYBLOCK_HUB --port 1");
        assertEquals("HypixelCore.jar", command.jar());
        assertEquals("SKYBLOCK_HUB", command.serverType());
        assertNull(ServiceCommand.parse("java -jar ServiceAPI.jar").serverType());
        assertNull(ServiceCommand.parse("sh entrypoint.sh"), "no -jar means nothing to fingerprint");
    }

    @Test
    void containerFactsParseInspect() {
        String inspect = """
                {"Id":"abcdef1234567890","Name":"/hypixelcore_hub",
                 "Config":{"Hostname":"abcdef123456","Env":["SERVICE_CMD=java -Xmx2g -jar HypixelCore.jar SKYBLOCK_HUB"]},
                 "State":{"Running":true,"StartedAt":"2026-11-01T10:00:00Z"},
                 "HostConfig":{"Memory":0},
                 "NetworkSettings":{"Networks":{"hypixel_network":{"IPAddress":"172.18.0.7"}}}}
                """;
        ContainerFacts facts = ContainerFacts.from("x", JsonParser.parseString(inspect).getAsJsonObject());
        assertEquals("hypixelcore_hub", facts.name());
        assertTrue(facts.running());
        assertEquals("SKYBLOCK_HUB", facts.command().serverType());
        assertEquals(2 * GIB, facts.heapBytes());
        assertEquals("172.18.0.7", facts.ipAddress());
        assertTrue(facts.answersTo("abcdef123456"), "the default hostname is the short container id");
        assertFalse(facts.answersTo("someone-else"));
    }

    @Test
    void xmxUnitsParse() {
        assertEquals(512L * 1024 * 1024, ContainerFacts.parseXmx("java -Xmx512m -jar x.jar"));
        assertEquals(0, ContainerFacts.parseXmx("java -jar x.jar"));
    }

    // --- version skew ---

    @Test
    void storeHashIsTheReference() {
        List<JarSkew.Entry> entries = JarSkew.evaluate(List.of(
                new JarSkew.Fingerprint("hub", "HypixelCore.jar", true, A, null),
                new JarSkew.Fingerprint("island", "HypixelCore.jar", true, B, null)),
                Map.of("HypixelCore.jar", A));
        assertEquals(JarSkew.State.MATCH, entries.get(0).state());
        assertEquals(JarSkew.State.SKEW, entries.get(1).state());
        assertEquals(List.of("island"), JarSkew.skewedContainers(entries, "HypixelCore.jar"));
    }

    @Test
    void withoutAStoredCopyTheMajorityDecides() {
        List<JarSkew.Entry> entries = JarSkew.evaluate(List.of(
                new JarSkew.Fingerprint("hub", "HypixelCore.jar", true, A, null),
                new JarSkew.Fingerprint("island", "HypixelCore.jar", true, A, null),
                new JarSkew.Fingerprint("garden", "HypixelCore.jar", true, B, null)), Map.of());
        assertEquals(JarSkew.State.SKEW, entries.get(2).state());
        assertEquals(JarSkew.State.MATCH, entries.get(0).state());
    }

    @Test
    void aLoneJarWithNoStoredCopyHasNoReference() {
        List<JarSkew.Entry> entries = JarSkew.evaluate(List.of(
                new JarSkew.Fingerprint("service_api", "ServiceAPI.jar", true, A, null)), Map.of());
        assertEquals(JarSkew.State.NO_REFERENCE, entries.get(0).state());
    }

    @Test
    void stoppedAndUnreadableAreNeverCalledSkew() {
        List<JarSkew.Entry> entries = JarSkew.evaluate(List.of(
                new JarSkew.Fingerprint("hub", "HypixelCore.jar", false, null, null),
                new JarSkew.Fingerprint("island", "HypixelCore.jar", true, null, "exec failed")),
                Map.of("HypixelCore.jar", A));
        assertEquals(JarSkew.State.STOPPED, entries.get(0).state());
        assertEquals(JarSkew.State.UNREADABLE, entries.get(1).state());
    }

    // --- config drift ---

    @Test
    void sha256sumOutputIsMadeRelative() {
        String output = A + "  /app/configuration/items/sword.yml\n"
                + B + " */app/configuration/config.yml\n"
                + "sha256sum: /app/configuration/x: Permission denied\n";
        Map<String, String> parsed = ConfigDrift.parseSha256sum(output, "/app/configuration");
        assertEquals(Map.of("items/sword.yml", A, "config.yml", B), parsed);
    }

    @Test
    void driftClassifiesEachDifference() {
        Map<String, String> host = Map.of("a.yml", A, "b.yml", A, "config.yml", A);
        Map<String, String> container = Map.of("a.yml", B, "c.yml", A, "config.yml", B);
        List<ConfigDrift.FileDrift> drift = ConfigDrift.compare(host, container,
                ConfigDrift.scopeFor(""), Set.of("config.yml"));

        assertEquals(List.of(
                new ConfigDrift.FileDrift("a.yml", ConfigDrift.Kind.CHANGED),
                new ConfigDrift.FileDrift("b.yml", ConfigDrift.Kind.MISSING_IN_CONTAINER),
                new ConfigDrift.FileDrift("c.yml", ConfigDrift.Kind.EXTRA_IN_CONTAINER)), drift);
        assertFalse(drift.get(2).syncable(), "a host deletion cannot be fixed by copying");
    }

    @Test
    void packServerScopeIgnoresTheRestOfTheTree() {
        List<ConfigDrift.FileDrift> drift = ConfigDrift.compare(
                Map.of("items/a.yml", A, "resourcepacks/skyblockpack/x.png", A),
                Map.of("resourcepacks/skyblockpack/x.png", B),
                ConfigDrift.scopeFor("resourcepacks"), Set.of());
        assertEquals(1, drift.size());
        assertEquals("resourcepacks/skyblockpack/x.png", drift.get(0).path());
    }

    @Test
    void longTarPathsSplitIntoPrefixAndName() {
        String path = "resourcepacks/skyblockpack/assets/minecraft/textures/item/" + "x".repeat(60) + ".png";
        String[] split = TarWriter.splitPath(path);
        assertEquals(path, split[0] + "/" + split[1]);
        assertTrue(split[1].length() <= 99);
        assertThrows(IllegalArgumentException.class, () -> TarWriter.splitPath("y".repeat(120)));

        byte[] tar = TarWriter.files(Map.of(path, "hi".getBytes(StandardCharsets.UTF_8)));
        assertEquals(0, tar.length % 512);
        assertEquals(split[0], new String(tar, 345, split[0].length(), StandardCharsets.UTF_8));
    }

    // --- capacity ---

    @Test
    void startingPastTheBudgetIsFlagged() {
        CapacityBudget.Budget budget = CapacityBudget.evaluate(32 * GIB, 4 * GIB, GIB, List.of(
                new CapacityBudget.Container("hub", true, 20 * GIB, 0),
                new CapacityBudget.Container("garden", false, 6 * GIB, 0),
                new CapacityBudget.Container("island", false, 6 * GIB, 0)), Set.of("garden", "island"));
        assertEquals(28 * GIB, budget.budgetBytes());
        assertEquals(20 * GIB, budget.committedBytes());
        assertEquals(12 * GIB, budget.candidateBytes());
        assertTrue(budget.overBudget());
        assertTrue(budget.summary().contains("over budget"));
    }

    @Test
    void unlimitedContainersAreEstimatedFromHeapOrFlat() {
        CapacityBudget.Budget budget = CapacityBudget.evaluate(32 * GIB, 4 * GIB, GIB, List.of(
                new CapacityBudget.Container("hub", true, 0, 4 * GIB),
                new CapacityBudget.Container("api", true, 0, 0)), Set.of());
        assertEquals(5 * GIB + GIB, budget.committedBytes());
        assertEquals(2, budget.estimatedCount());
        assertFalse(budget.overBudget());
    }

    // --- resource pack ---

    @Test
    void packHashesAreReadFromBootLogs() {
        String sha = "0123456789abcdef0123456789abcdef01234567";
        assertEquals(sha, PackVerdict.advertisedHash(
                "2026 INFO Resource pack 'skyblockpack' built. Hash: " + sha, "skyblockpack"));
        assertNull(PackVerdict.advertisedHash("Resource pack 'other' built. Hash: " + sha, "skyblockpack"));
        assertEquals(Set.of(sha), PackVerdict.servedHashes("Pack URL: http://0.0.0.0:7270/" + sha + ".zip"));
    }

    @Test
    void packVerdicts() {
        String one = "1".repeat(40);
        String two = "2".repeat(40);
        HttpProbe.Outcome served = new HttpProbe.Outcome("u", 200, null);
        HttpProbe.Outcome missing = new HttpProbe.Outcome("u", 404, null);

        assertEquals(PackVerdict.State.DEAD,
                PackVerdict.evaluate(false, false, Map.of(), Map.of(), Map.of()).state());
        assertEquals(PackVerdict.State.LIVE,
                PackVerdict.evaluate(true, true, Map.of("hub", one), Map.of(one, served), Map.of()).state());
        assertEquals(PackVerdict.State.STALE,
                PackVerdict.evaluate(true, true, Map.of("hub", one), Map.of(one, missing), Map.of()).state());
        assertEquals(PackVerdict.State.STALE, PackVerdict.evaluate(true, true,
                Map.of("hub", one, "island", two), Map.of(one, served, two, served), Map.of()).state(),
                "game servers disagreeing kicks whoever lands on the odd one out");
    }

    // --- proxy registration alerts ---

    @Test
    void delistedBecomesAnAlertOnlyWhileTheReportIsFresh() {
        Instant now = Instant.now();
        assertEquals(Set.of("delisted:hypixelcore_hub"),
                RegistrationReport.conditions(now, true, List.of("hypixelcore_hub"), now).keySet());
        assertTrue(RegistrationReport.conditions(now.minus(Duration.ofMinutes(10)), true,
                List.of("hypixelcore_hub"), now).isEmpty(), "a stale report must not keep an alert alive");
        assertTrue(RegistrationReport.conditions(now, false, List.of("hypixelcore_hub"), now).isEmpty(),
                "an unreachable proxy is the proxy's own alert, not one per server");
    }

    // --- post-restart verification ---

    @Test
    void bootVerdicts() {
        assertNull(BootVerifier.judge(null, A), "first boot has nothing to verify");
        assertNull(BootVerifier.judge(new BootVerifier.State(A, Instant.now(), null, null, null), A));

        BootVerifier.Verdict swapped = BootVerifier.judge(
                new BootVerifier.State(A, Instant.now(), null, null, null), B);
        assertNotNull(swapped);
        assertTrue(swapped.detail().contains("jar changed"));

        BootVerifier.Verdict restarted = BootVerifier.judge(
                new BootVerifier.State(A, Instant.now(), "restart", "owner (dev1)", Instant.now()), A);
        assertNotNull(restarted);
        assertEquals("owner (dev1)", restarted.requestedBy());
    }
}
