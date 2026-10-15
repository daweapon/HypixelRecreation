package dev.daweapon.type.skyblockgeneric.garden.chip;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChipServiceTest {
    @Test
    void levelCostsMatchTheWikiTable() {
        long total = 0;
        for (int level = 2; level <= 10; level++) total += ChipService.costToReach(level);
        assertEquals(5_250_000, total);
        for (int level = 11; level <= 15; level++) total += ChipService.costToReach(level);
        assertEquals(13_250_000, total);
        for (int level = 16; level <= 20; level++) total += ChipService.costToReach(level);
        assertEquals(25_000_000, total);
    }

    @Test
    void rarityFollowsRedeemedCopies() {
        assertEquals(-1, GardenChip.rarityIndex(0));
        assertEquals(0, GardenChip.rarityIndex(1));
        assertEquals(1, GardenChip.rarityIndex(5));
        assertEquals(2, GardenChip.rarityIndex(21));
    }

    @Test
    void bonusScalesWithLevelAndRarity() {
        ChipData data = new ChipData();
        data.setConsumed(GardenChip.CROPSHOT, 21);
        data.setLevel(GardenChip.CROPSHOT, 20);
        assertEquals(100, ChipService.bonus(data, GardenChip.CROPSHOT));
        data.setConsumed(GardenChip.EVERGREEN, 1);
        data.setLevel(GardenChip.EVERGREEN, 10);
        assertEquals(20, ChipService.bonus(data, GardenChip.EVERGREEN));
        assertEquals(0, ChipService.bonus(data, GardenChip.OVERDRIVE));
        assertTrue(ChipData.fromJson(data.toJson()).unlocked(GardenChip.CROPSHOT));
    }
}
