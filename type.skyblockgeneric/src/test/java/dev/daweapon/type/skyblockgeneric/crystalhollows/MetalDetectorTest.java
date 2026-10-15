package dev.daweapon.type.skyblockgeneric.crystalhollows;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetalDetectorTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static boolean isScavenged(MetalDetectorLoot.Entry entry) {
        return entry.item() != null && MetalDetectorLoot.SCAVENGED.contains(entry.item());
    }

    @Test
    void scavengedItemsAreOnlyOfferedWhileMissingAndShareTheTwentyPercent() {
        List<MetalDetectorLoot.Entry> all = MetalDetectorLoot.table(MetalDetectorLoot.SCAVENGED);
        double scavenged = all.stream().filter(e -> isScavenged(e)).mapToDouble(MetalDetectorLoot.Entry::weight).sum();
        assertEquals(20, scavenged, 1e-9);
        List<MetalDetectorLoot.Entry> none = MetalDetectorLoot.table(List.of());
        assertTrue(none.stream().noneMatch(e -> isScavenged(e)));
        List<MetalDetectorLoot.Entry> one = MetalDetectorLoot.table(List.of(ItemType.DWARVEN_GOLD_HAMMER));
        assertEquals(20, one.stream().filter(e -> e.item() == ItemType.DWARVEN_GOLD_HAMMER).findFirst().orElseThrow().weight(), 1e-9);
    }

    @Test
    void tableSwitchesToTheHasScavengedColumnOnceAllFourAreFound() {
        double missing = MetalDetectorLoot.table(MetalDetectorLoot.SCAVENGED).stream().filter(e -> e.item() == ItemType.FLAWED_JADE_GEM && e.min() == 2).findFirst().orElseThrow().weight();
        double complete = MetalDetectorLoot.table(List.of()).stream().filter(e -> e.item() == ItemType.FLAWED_JADE_GEM && e.min() == 2).findFirst().orElseThrow().weight();
        assertEquals(5.95, missing, 1e-9);
        assertEquals(7.44, complete, 1e-9);
    }

    @Test
    void gemAmountsFollowTheWiki() {
        List<MetalDetectorLoot.Entry> table = MetalDetectorLoot.table(List.of());
        assertTrue(table.stream().anyMatch(e -> e.item() == ItemType.ROUGH_JASPER_GEM && e.min() == 40 && e.max() == 40));
        assertTrue(table.stream().anyMatch(e -> e.item() == ItemType.FINE_RUBY_GEM && e.min() == 4));
        assertTrue(table.stream().anyMatch(e -> e.item() == ItemType.FLAWED_AMBER_GEM && e.min() == 1));
        assertEquals(3, table.stream().filter(MetalDetectorLoot.Entry::powder).count());
    }

    @Test
    void rollingEventuallyDropsEveryMissingScavengedItem() {
        Random random = new Random(7);
        Set<ItemType> seen = new HashSet<>();
        List<MetalDetectorLoot.Entry> table = MetalDetectorLoot.table(MetalDetectorLoot.SCAVENGED);
        for (int i = 0; i < 2_000; i++) {
            MetalDetectorLoot.Entry entry = MetalDetectorLoot.roll(java.util.UUID.randomUUID(), table, random);
            assertNotNull(entry);
            if (entry.item() != null && MetalDetectorLoot.SCAVENGED.contains(entry.item())) seen.add(entry.item());
        }
        assertEquals(Set.copyOf(MetalDetectorLoot.SCAVENGED), seen);
    }

    @Test
    void nearestSpotSkipsClearedOnesAndRespectsRange() {
        List<Pos> spots = List.of(new Pos(0, 0, 0), new Pos(10, 0, 0), new Pos(100, 0, 0));
        MetalDetector.Reading first = MetalDetector.nearest(spots, Set.of(), new Pos(1, 0, 0), 30);
        assertEquals(0, first.index());
        MetalDetector.Reading second = MetalDetector.nearest(spots, Set.of(0), new Pos(1, 0, 0), 30);
        assertEquals(1, second.index());
        assertNull(MetalDetector.nearest(spots, Set.of(0, 1), new Pos(1, 0, 0), 30));
    }

    @Test
    void beepsGetFasterAndHigherPitchedWhenCloser() {
        assertTrue(MetalDetector.beepIntervalTicks(2) < MetalDetector.beepIntervalTicks(25));
        assertTrue(MetalDetector.beepPitch(2) > MetalDetector.beepPitch(25));
        assertEquals(4, MetalDetector.beepIntervalTicks(0));
    }
}
