package net.swofty.type.skyblockgeneric.minion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinionCompactionTest {
    @Test
    void oneEnchantedItemCostsOneHundredSixtyBase() {
        assertEquals(0, MinionCompaction.enchantedCount(159));
        assertEquals(1, MinionCompaction.enchantedCount(160));
        assertEquals(3, MinionCompaction.enchantedCount(500));
        assertEquals(0, MinionCompaction.enchantedCount(-5));
    }
}
