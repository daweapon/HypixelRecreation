package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitHitman;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitHitmanTest {
    private static final long MINUTE = 60_000L;
    private static final long NOW = 1_000_000_000L;

    private static ChocolateFactoryData withSlots(int slots) {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.setHitmanSlots(slots);
        return data;
    }

    @Test
    void twoOpeningsQueueTwentyThenFortyMinutes() {
        ChocolateFactoryData data = withSlots(3);
        RabbitHitman.startCooldown(data, NOW, 0);
        assertEquals(20 * MINUTE, data.getHitmanQueueEnd() - NOW);
        RabbitHitman.startCooldown(data, NOW, 0);
        assertEquals(40 * MINUTE, data.getHitmanQueueEnd() - NOW);
        assertEquals(2, RabbitHitman.slotsOnCooldown(data, NOW, 0));
    }

    @Test
    void shardShortensCooldown() {
        assertEquals(15 * MINUTE, RabbitHitman.cooldownPerSlot(25));
        ChocolateFactoryData data = withSlots(3);
        RabbitHitman.startCooldown(data, NOW, 25);
        assertEquals(15 * MINUTE, data.getHitmanQueueEnd() - NOW);
    }

    @Test
    void openingNeedsAnIdleSlot() {
        ChocolateFactoryData data = withSlots(1);
        data.getHitmanEggs().add("HUB:0");
        data.getHitmanEggs().add("HUB:1");
        assertTrue(RabbitHitman.canOpen(data, NOW, 0));
        RabbitHitman.startCooldown(data, NOW, 0);
        assertFalse(RabbitHitman.canOpen(data, NOW, 0));
        assertTrue(RabbitHitman.canOpen(data, NOW + 21 * MINUTE, 0));
    }

    @Test
    void freeSlotsCountEggsAndCooldowns() {
        ChocolateFactoryData data = withSlots(4);
        data.getHitmanEggs().add("HUB:0");
        RabbitHitman.startCooldown(data, NOW, 0);
        assertEquals(2, RabbitHitman.freeSlots(data, NOW, 0));
        assertEquals(3, RabbitHitman.freeSlots(data, NOW + 21 * MINUTE, 0));
    }

    @Test
    void priceListIsIncreasingAndSumsToWikiTotal() {
        assertEquals(292_111_000L, RabbitHitman.totalPrice());
        for (int i = 1; i < RabbitHitman.MAX_SLOTS; i++) {
            assertTrue(RabbitHitman.slotPrice(i) > RabbitHitman.slotPrice(i - 1));
        }
    }

    @Test
    void slotsAreLockedByFactoryLevel() {
        ChocolateFactoryData data = withSlots(6);
        assertEquals(6, RabbitHitman.unlockedSlots(data));
        assertFalse(RabbitHitman.canBuyNextSlot(data));
        data.setPrestigeLevel(1);
        assertTrue(RabbitHitman.canBuyNextSlot(data));
        assertEquals(1, RabbitHitman.requiredFactoryLevel(5));
        assertEquals(2, RabbitHitman.requiredFactoryLevel(6));
        assertEquals(6, RabbitHitman.requiredFactoryLevel(27));
        data.setPrestigeLevel(5);
        data.setHitmanSlots(28);
        assertFalse(RabbitHitman.canBuyNextSlot(data));
    }

    @Test
    void missedSpawnsSkipClaimedAndRespectFreeSlots() {
        Set<String> claimed = Set.of("3:12", "3:14");
        List<Integer> missed = RabbitHitman.missedSpawns(3, 16, claimed, 9, 10);
        assertEquals(List.of(10, 11, 13, 15), missed);
        assertEquals(List.of(10, 11), RabbitHitman.missedSpawns(3, 16, claimed, 9, 2));
        assertTrue(RabbitHitman.missedSpawns(3, 16, claimed, 15, 5).isEmpty());
    }

    @Test
    void catchUpIsLimitedToTheTrackedClaimWindow() {
        List<Integer> missed = RabbitHitman.missedSpawns(3, 200, Set.of(), -1, 100);
        assertEquals(RabbitHitman.CATCH_UP_SPAWNS, missed.size());
        assertEquals(160, missed.getFirst());
    }

    @Test
    void lastCheckedMarkerIsPerYear() {
        ChocolateFactoryData data = withSlots(1);
        data.setHitmanLastCheckedSpawn(RabbitHitman.markerOf(2, 77));
        assertEquals(77, RabbitHitman.lastChecked(data, 2));
        assertEquals(-1, RabbitHitman.lastChecked(data, 3));
    }
}
