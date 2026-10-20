package dev.daweapon.type.skyblockgeneric.torrhus;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HoneyhivesTest {
    @Test
    void thirtyHiveSitesComeFromTheCapture() {
        List<Pos> positions = Honeyhives.positions(Path.of("../configuration/skyblock/torrhus/honeyhives.yml"));
        assertEquals(30, positions.size());
        assertTrue(positions.contains(new Pos(-733, 128, 188)));
    }

    @Test
    void lootTableWeightsMatchTheWiki() {
        int total = 0;
        for (HoneyhiveLoot.Kind kind : HoneyhiveLoot.Kind.values()) total += kind.weight();
        assertEquals(480, total);
        assertEquals(10, HoneyhiveLoot.Kind.QUEEN_BEE_SHARD.weight());
    }

    @Test
    void coinsAndHoneycombStayInRange() {
        Random random = new Random(7);
        for (int i = 0; i < 500; i++) {
            int honeycomb = HoneyhiveLoot.honeycomb(random);
            assertTrue(honeycomb >= 16 && honeycomb <= 64);
            int small = HoneyhiveLoot.Kind.SMALL_COINS.rollCoins(random);
            assertTrue(small >= 1000 && small <= 2500);
            int large = HoneyhiveLoot.Kind.LARGE_COINS.rollCoins(random);
            assertTrue(large >= 2500 && large <= 5000);
        }
        assertEquals(0, HoneyhiveLoot.Kind.HONEYBUZZ_SHARD.rollCoins(random));
    }

    @Test
    void cooldownLastsAnHourAndTheHologramShowsIt() {
        assertEquals(0, Honeyhives.millisLeft((Long) null, 0));
        assertEquals(3_000_000L, Honeyhives.millisLeft(0L, 600_000L));
        assertEquals(0, Honeyhives.millisLeft(0L, 3_600_001L));
        assertArrayEquals(new String[]{"<6>Honeyhive", "<a>Click to loot!"}, Honeyhives.hologramText(0));
        assertEquals("<6>Honeyhive", Honeyhives.hologramText(1000)[0]);
        assertEquals(3_600_000L, Honeyhives.COOLDOWN_MILLIS);
    }
}
