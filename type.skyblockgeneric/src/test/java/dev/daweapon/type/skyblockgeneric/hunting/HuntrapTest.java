package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntrapTest {
    private static Set<String> ids(ServerType world, RegionType region, HuntrapMedium medium) {
        return TrapPools.candidates(world, region, medium).stream().map(entry -> entry.shard().toString()).collect(Collectors.toSet());
    }

    @Test
    void catchTimeIsBoundedByTheShardRarity() {
        assertEquals(Duration.ofHours(8), HuntrapTiming.minimum(AttributeRarity.COMMON));
        assertEquals(Duration.ofHours(12), HuntrapTiming.maximum(AttributeRarity.COMMON));
        assertEquals(Duration.ofHours(16), HuntrapTiming.minimum(AttributeRarity.LEGENDARY));
        assertEquals(Duration.ofHours(24), HuntrapTiming.maximum(AttributeRarity.LEGENDARY));
        assertEquals(Duration.ofHours(8).toMillis(), HuntrapTiming.millis(AttributeRarity.COMMON, HuntrapTier.RETIA_BASICA, 0, 0));
        assertEquals(Duration.ofHours(12).toMillis(), HuntrapTiming.millis(AttributeRarity.COMMON, HuntrapTier.RETIA_BASICA, 0, 1));
    }

    @Test
    void trapTierComesFirstThenTheAdditiveDiscounts() {
        assertEquals(0.5, HuntrapTiming.multiplier(HuntrapTier.RETIA_SUPREMA, 0), 1e-9);
        assertEquals(0.325, HuntrapTiming.multiplier(HuntrapTier.RETIA_SUPREMA, 0.25 + 0.10), 1e-9);
        assertEquals(0.375, HuntrapTiming.multiplier(HuntrapTier.RETIA_SUPREMA, 0.25), 1e-9);
        assertEquals(0.65, HuntrapTiming.multiplier(HuntrapTier.RETIA_FORTA, 0), 1e-9);
        assertEquals(0.9, HuntrapTiming.multiplier(HuntrapTier.RETIA_MELIORA, 0), 1e-9);
        assertEquals(1, HuntrapTiming.multiplier(HuntrapTier.RETIA_BASICA, 0), 1e-9);
    }

    @Test
    void everyPoolEntryIsARealShardAndWeightsArePositive() {
        for (TrapPools.Pool pool : TrapPools.pools())
            for (TrapPools.Entry entry : pool.entries()) {
                assertNotNull(AttributeRegistry.get(entry.shard()), pool.id() + " " + entry.shard());
                assertTrue(entry.weight() > 0, pool.id());
            }
    }

    @Test
    void backwaterBayouUnderwaterCatchesTheWikiShardsAndNothingOnLand() {
        assertEquals(Set.of("C8", "U44", "E6", "L47"), ids(ServerType.SKYBLOCK_BACKWATER_BAYOU, null, HuntrapMedium.UNDERWATER));
        assertFalse(ids(ServerType.SKYBLOCK_BACKWATER_BAYOU, null, HuntrapMedium.LAND).contains("C8"));
    }

    @Test
    void theFishingAndTrapShardsHaveTheirWikiLocations() {
        assertTrue(ids(ServerType.SKYBLOCK_DWARVEN_MINES, RegionType.DWARVEN_MINES, HuntrapMedium.UNDERWATER).contains("U26"));
        assertTrue(ids(ServerType.SKYBLOCK_TORRHUS_CANYON, RegionType.HOTSPOT_HAVEN, HuntrapMedium.UNDERWATER).containsAll(Set.of("R83", "R89")));
        assertTrue(ids(ServerType.SKYBLOCK_TORRHUS_CANYON, RegionType.SPRING_DEPTHS, HuntrapMedium.UNDERWATER).contains("R83"));
        assertFalse(ids(ServerType.SKYBLOCK_TORRHUS_CANYON, RegionType.SPRING_SHALLOWS, HuntrapMedium.UNDERWATER).contains("R83"));
    }

    @Test
    void regionPoolsNeedTheirRegion() {
        assertTrue(ids(ServerType.SKYBLOCK_DWARVEN_MINES, RegionType.THE_MIST, HuntrapMedium.LAND).contains("E33"));
        assertFalse(ids(ServerType.SKYBLOCK_DWARVEN_MINES, RegionType.DWARVEN_VILLAGE, HuntrapMedium.LAND).contains("E33"));
        assertTrue(ids(ServerType.SKYBLOCK_ISLAND, null, HuntrapMedium.LAND).isEmpty());
    }

    @Test
    void mediumDetection() {
        assertEquals(HuntrapMedium.UNDERWATER, HuntrapMedium.of(true, false));
        assertEquals(HuntrapMedium.UNDER_LAVA, HuntrapMedium.of(false, true));
        assertEquals(HuntrapMedium.LAND, HuntrapMedium.of(false, false));
        assertTrue(HuntrapMedium.ANY.accepts(HuntrapMedium.LAND));
        assertFalse(HuntrapMedium.LAND.accepts(HuntrapMedium.UNDERWATER));
    }

    @Test
    void toolkitHoldsTwentyOneToolsAndAllFiveTraps() {
        assertEquals(21, HuntingToolkit.SLOTS.size());
        List<HuntingToolkit.Slot> traps = HuntingToolkit.SLOTS.stream().filter(slot -> slot.trap() != null).toList();
        assertEquals(5, traps.size());
        assertEquals(HuntrapTier.values().length, traps.size());
        assertEquals(21, HuntingToolkit.SLOTS.stream().map(HuntingToolkit.Slot::key).distinct().count());
        assertEquals(HuntingToolkit.Group.LASSO, HuntingToolkit.slotOf("VINERIP_LASSO").group());
    }
}
