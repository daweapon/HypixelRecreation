package dev.daweapon.service.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.daweapon.service.api.control.DeployVerdicts;
import dev.daweapon.service.api.control.ProxyRegistrationProbe;
import dev.daweapon.service.api.control.ProxyRegistrations;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetIntegrityTest {
    private static final Instant NOW = Instant.parse("2026-11-03T12:00:00Z");
    private static final Instant LONG_AGO = NOW.minus(Duration.ofHours(2));

    private static ProxyRegistrations.Server server(String name, String type, String hostname, Instant startedAt) {
        return new ProxyRegistrations.Server(name, "UP", startedAt, hostname + "0000", hostname, type);
    }

    private static ProxyRegistrations.Registration registration(String type, String host) {
        return new ProxyRegistrations.Registration("mini1A", "uuid", type, 20000, 3, host);
    }

    private static ProxyRegistrations.State stateOf(ProxyRegistrations.Result result, String container) {
        return result.rows().stream().filter(row -> row.container().equals(container)).findFirst().orElseThrow().state();
    }

    // --- proxy registration ---

    @Test
    void registrationsMatchByHostname() {
        ProxyRegistrations.Result result = ProxyRegistrations.match(List.of(
                        server("hypixelcore_hub", "SKYBLOCK_HUB", "aaa111", LONG_AGO),
                        server("hypixelcore_hub_2", "SKYBLOCK_HUB", "bbb222", LONG_AGO)),
                List.of(registration("SKYBLOCK_HUB", "bbb222")), null, NOW);

        assertEquals(ProxyRegistrations.State.DELISTED, stateOf(result, "hypixelcore_hub"),
                "with hosts reported, the second hub's registration must not cover the first");
        assertEquals(ProxyRegistrations.State.REGISTERED, stateOf(result, "hypixelcore_hub_2"));
        assertEquals(List.of("hypixelcore_hub"), result.delisted());
        assertEquals("CRITICAL", result.severity());
    }

    @Test
    void anOlderProxyWithoutHostsFallsBackToType() {
        ProxyRegistrations.Result result = ProxyRegistrations.match(List.of(
                        server("hypixelcore_hub", "SKYBLOCK_HUB", "aaa111", LONG_AGO)),
                List.of(registration("SKYBLOCK_HUB", null)), null, NOW);
        assertEquals(ProxyRegistrations.State.REGISTERED, stateOf(result, "hypixelcore_hub"));
        assertEquals("NOMINAL", result.severity());
    }

    @Test
    void aFreshlyStartedServerIsBootingNotDelisted() {
        ProxyRegistrations.Result result = ProxyRegistrations.match(List.of(
                server("hypixelcore_hub", "SKYBLOCK_HUB", "aaa111", NOW.minusSeconds(30))), List.of(), null, NOW);
        assertEquals(ProxyRegistrations.State.BOOTING, stateOf(result, "hypixelcore_hub"));
        assertTrue(result.delisted().isEmpty());
    }

    @Test
    void anUnreachableProxyIsUnknownNeverDelisted() {
        ProxyRegistrations.Result result = ProxyRegistrations.match(List.of(
                server("hypixelcore_hub", "SKYBLOCK_HUB", "aaa111", LONG_AGO),
                new ProxyRegistrations.Server("hypixelcore_island", "EXIT", null, "x", null, "SKYBLOCK_ISLAND")),
                null, "TimeoutException", NOW);
        assertFalse(result.proxyReachable());
        assertEquals(ProxyRegistrations.State.UNKNOWN, stateOf(result, "hypixelcore_hub"));
        assertEquals(ProxyRegistrations.State.NOT_RUNNING, stateOf(result, "hypixelcore_island"));
        assertTrue(result.delisted().isEmpty());
    }

    @Test
    void proxyServerListParses() {
        List<ProxyRegistrations.Registration> parsed = ProxyRegistrationProbe.parse("""
                {"servers":[{"name":"mega1A","uuid":"u","type":"SKYBLOCK_HUB","port":20001,
                  "players":["p1","p2"],"max_players":80,"short_name":"M1A","host":"aaa111"},
                 {"name":"L2b","uuid":"v","type":"PROTOTYPE_LOBBY","port":20002,"players":[],
                  "max_players":20,"short_name":"L2b"}]}
                """);
        assertEquals(2, parsed.size());
        assertEquals(2, parsed.get(0).players());
        assertEquals("aaa111", parsed.get(0).host());
        assertNull(parsed.get(1).host(), "a proxy without the host field still parses");
    }

    // --- post-restart verdicts ---

    @Test
    void finishedDeploysBecomeVerdictsOnce() {
        JsonArray jobs = JsonParser.parseString("""
                [{"id":"j1","operation":"deploy:ServiceAPI.jar","status":"SUCCEEDED",
                  "finishedAt":"2026-11-03T11:59:00Z","steps":[{"container":"service_api","action":"probe","ok":true,"message":"ok"}]},
                 {"id":"j2","operation":"deploy:HypixelCore.jar","status":"COMPLETED_WITH_ERRORS","currentStep":"done",
                  "finishedAt":"2026-11-03T11:58:00Z","steps":[{"container":"hypixelcore_hub","action":"health","ok":false,"message":"exited with code 1"}]},
                 {"id":"j3","operation":"restart","status":"SUCCEEDED","finishedAt":"2026-11-03T11:58:00Z","steps":[]},
                 {"id":"j4","operation":"deploy:ServiceParty.jar","status":"RUNNING","steps":[]},
                 {"id":"j5","operation":"verify:ServiceControl.jar","status":"SUCCEEDED","finishedAt":"2026-11-03T11:57:00Z","steps":[]}]
                """).getAsJsonArray();

        List<DeployVerdicts.Verdict> verdicts = DeployVerdicts.pending(jobs, Set.of("j5"), NOW, Duration.ofHours(6));
        assertEquals(List.of("j1", "j2"), verdicts.stream().map(DeployVerdicts.Verdict::jobId).toList());
        assertTrue(verdicts.get(0).ok());
        assertFalse(verdicts.get(1).ok());
        assertTrue(verdicts.get(1).detail().contains("hypixelcore_hub health: exited with code 1"));
        assertEquals("j1", verdicts.get(0).targets().getFirst(), "the job id leads, so it can be de-duplicated");
    }
}
