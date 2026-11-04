package dev.daweapon.service.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.metrics.ServerMetrics;
import dev.daweapon.service.api.control.DrainRestart;
import dev.daweapon.service.api.control.MetricsReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveGameDataTest {
    private static final long NOW = 5_000_000L;

    private static ServerMetrics server(String name, String host, double tps, int players) {
        return new ServerMetrics("uuid-" + name, name, name, ServerType.SKYBLOCK_HUB, host,
                tps, 10, 1000, 4000, players, 20, NOW);
    }

    @Test
    void theSummaryNamesTheSlowestServerAndAddsUpPlayersAndHeap() {
        JsonObject report = MetricsReport.build(List.of(
                server("mini1A", "aaaa", 19.9, 4),
                server("mini2B", "bbbb", 16.0, 6)), NOW);
        JsonObject summary = report.getAsJsonObject("summary");

        assertEquals(2, summary.get("servers").getAsInt());
        assertEquals(10, summary.get("players").getAsInt());
        assertEquals(2000, summary.get("heapUsedMb").getAsLong());
        assertEquals(16.0, summary.get("lowestTps").getAsDouble());
        assertEquals("mini2B", summary.get("lowestTpsServer").getAsString());
        assertEquals("LOADED", summary.get("worstHealth").getAsString());
        assertEquals(1, summary.get("loaded").getAsInt());
        assertEquals("LOADED", report.getAsJsonArray("servers").get(1).getAsJsonObject().get("health").getAsString());
    }

    @Test
    void anEmptyFleetReportsNominalWithNoLowestTps() {
        JsonObject summary = MetricsReport.build(List.of(), NOW).getAsJsonObject("summary");

        assertEquals("NOMINAL", summary.get("worstHealth").getAsString());
        assertFalse(summary.has("lowestTps"));
    }

    @Test
    void drainTimeoutsAreClampedToASaneWindow() {
        assertEquals(DrainRestart.DEFAULT_TIMEOUT_SECONDS, DrainRestart.clampTimeout(0));
        assertEquals(DrainRestart.MIN_TIMEOUT_SECONDS, DrainRestart.clampTimeout(1));
        assertEquals(DrainRestart.MAX_TIMEOUT_SECONDS, DrainRestart.clampTimeout(9999));
        assertEquals(60, DrainRestart.clampTimeout(60));
    }

    @Test
    void theContainerIsFoundByNameAndItsServerByHostname() {
        JsonObject fleet = JsonParser.parseString("""
                {"containers":[{"id":"aaaa1111","name":"hypixelcore_hub"},{"id":"bbbb2222","name":"hypixelcore_island"}]}
                """).getAsJsonObject();

        String id = DrainRestart.containerId(fleet, "hypixelcore_island");
        assertEquals("bbbb2222", id);
        assertNull(DrainRestart.containerId(fleet, "nope"));

        List<ServerMetrics> heartbeats = List.of(server("mini1A", "aaaa", 20, 1), server("mini2B", "bbbb", 20, 1));
        assertEquals("mini2B", DrainRestart.serverFor(heartbeats, id, "hypixelcore_island").orElseThrow().serverName());
        assertTrue(DrainRestart.serverFor(heartbeats, "cccc", "other").isEmpty());
    }
}
