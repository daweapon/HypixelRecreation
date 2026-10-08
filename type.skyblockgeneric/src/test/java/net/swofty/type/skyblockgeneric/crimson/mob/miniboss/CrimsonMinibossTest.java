package net.swofty.type.skyblockgeneric.crimson.mob.miniboss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.crimson.mob.MobDrop;
import net.swofty.type.skyblockgeneric.loottable.ForcedDrops;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonMinibossTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void dailyReputationResetsAtMidnightUtc() {
        long lateTonight = CrimsonMinibossMob.dailyMarker(Instant.parse("2026-10-08T23:59:59Z"));
        long earlyToday = CrimsonMinibossMob.dailyMarker(Instant.parse("2026-10-08T00:00:00Z"));
        long tomorrow = CrimsonMinibossMob.dailyMarker(Instant.parse("2026-10-09T00:00:00Z"));
        assertEquals(earlyToday, lateTonight);
        assertEquals(lateTonight + 1, tomorrow);
    }

    @Test
    void onlyTheFirstKillOfTheDayPays() {
        long today = CrimsonMinibossMob.dailyMarker(Instant.parse("2026-10-08T12:00:00Z"));
        assertTrue(CrimsonMinibossMob.isFirstKillToday(0, today));
        assertFalse(CrimsonMinibossMob.isFirstKillToday(today, today));
        assertTrue(CrimsonMinibossMob.isFirstKillToday(today - 1, today));
        assertNotEquals(0, CrimsonMinibossMob.dailyMarker(Instant.EPOCH));
    }

    @Test
    void aSummonedBossFightsWhereItWasSummoned() {
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            assertTrue(CrimsonMinibossMob.isAtNaturalSpawn(miniboss.getSpawn(), miniboss));
            assertFalse(CrimsonMinibossMob.isAtNaturalSpawn(miniboss.getSpawn().add(40, 0, 0), miniboss));
            assertFalse(CrimsonMinibossMob.isAtNaturalSpawn(new Pos(-360.5, 80, -430.5), miniboss));
        }
    }

    @Test
    void everyMinibossDropCanBeForcedThroughLootRoll() {
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            for (MobDrop drop : miniboss.getDrops()) {
                List<ItemType> items = drop.availableItems();
                if (items.isEmpty()) continue;
                for (ItemType item : items) {
                    UUID player = UUID.randomUUID();
                    ForcedDrops.force(player, item);
                    assertEquals(item, CrimsonMobLoot.decide(player, items, 0, new Random(1)),
                            miniboss + " cannot force " + item);
                    assertTrue(ForcedDrops.pending(player).isEmpty());
                }
            }
        }
    }

    @Test
    void guaranteedDropsAlwaysLandAndZeroChanceNeverDoes() {
        UUID player = UUID.randomUUID();
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            List<ItemType> guaranteed = miniboss.getDrops().getFirst().availableItems();
            assertFalse(guaranteed.isEmpty(), miniboss + " has no guaranteed drop item");
            assertEquals(guaranteed.getFirst(), CrimsonMobLoot.decide(player, guaranteed, 100, new Random(2)));
            assertNull(CrimsonMobLoot.decide(player, guaranteed, 0, new Random(2)));
        }
    }
}
