package dev.daweapon.commons.metrics;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerMetricsTest {
    private static final long NOW = 1_000_000L;

    private static ServerMetrics metrics(double tps, double mspt, long heapUsed, long heapMax, long publishedAt) {
        return new ServerMetrics("uuid", "mini1A", "m1A", ServerType.SKYBLOCK_HUB, "0123456789ab",
                tps, mspt, heapUsed, heapMax, 3, 20, publishedAt);
    }

    @Test
    void aHealthyServerIsNominal() {
        assertEquals(ServerMetrics.Health.NOMINAL, metrics(20, 12, 1000, 4000, NOW).health(NOW));
    }

    @Test
    void lowTpsSlowTicksOrAFullHeapMarkAServerLoaded() {
        assertEquals(ServerMetrics.Health.LOADED, metrics(17.5, 12, 1000, 4000, NOW).health(NOW));
        assertEquals(ServerMetrics.Health.LOADED, metrics(20, 50, 1000, 4000, NOW).health(NOW));
        assertEquals(ServerMetrics.Health.LOADED, metrics(20, 12, 3500, 4000, NOW).health(NOW));
    }

    @Test
    void collapsedTpsOrAnExhaustedHeapIsFailing() {
        assertEquals(ServerMetrics.Health.FAILING, metrics(8, 12, 1000, 4000, NOW).health(NOW));
        assertEquals(ServerMetrics.Health.FAILING, metrics(20, 150, 1000, 4000, NOW).health(NOW));
        assertEquals(ServerMetrics.Health.FAILING, metrics(20, 12, 3900, 4000, NOW).health(NOW));
    }

    @Test
    void aHeartbeatThatStoppedArrivingIsFailingEvenIfItsLastNumbersWereFine() {
        long publishedAt = NOW - ServerMetrics.STALE_AFTER_MILLIS - 1;
        assertEquals(ServerMetrics.Health.FAILING, metrics(20, 12, 1000, 4000, publishedAt).health(NOW));
    }

    @Test
    void anUnknownHeapLimitDoesNotCountAsFull() {
        assertEquals(0, metrics(20, 12, 1000, 0, NOW).heapRatio());
    }

    @Test
    void matchesTheContainerWhoseIdStartsWithTheHostname() {
        ServerMetrics heartbeat = metrics(20, 12, 1000, 4000, NOW);
        assertTrue(heartbeat.belongsToContainer("0123456789abcdef0011", "hypixelcore_hub"));
        assertFalse(heartbeat.belongsToContainer("ffff456789abcdef0011", "hypixelcore_hub"));
    }

    @Test
    void survivesTheRedisRoundTrip() {
        JacksonSerializer<ServerMetrics> serializer = new JacksonSerializer<>(ServerMetrics.class);
        ServerMetrics original = metrics(19.6, 14.2, 1234, 4096, NOW);
        assertEquals(original, serializer.deserialize(serializer.serialize(original)));
    }

    @Test
    void drainSendsSkyBlockPlayersToTheHubAndHubPlayersHome() {
        assertEquals(ServerType.SKYBLOCK_HUB, DrainDestination.of(ServerType.SKYBLOCK_CRIMSON_ISLE));
        assertEquals(ServerType.SKYBLOCK_ISLAND, DrainDestination.of(ServerType.SKYBLOCK_HUB));
        assertEquals(ServerType.PROTOTYPE_LOBBY, DrainDestination.of(ServerType.BEDWARS_LOBBY));
        assertEquals(ServerType.SKYBLOCK_HUB, DrainDestination.of(ServerType.PROTOTYPE_LOBBY));
    }
}
