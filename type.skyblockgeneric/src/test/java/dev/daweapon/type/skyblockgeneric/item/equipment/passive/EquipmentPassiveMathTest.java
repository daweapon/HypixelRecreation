package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentPassiveMathTest {
    @Test
    void vanquishedBeltReflectsMoreAndTheBestBeltWins() {
        assertEquals(10, EquipmentPassiveMath.reflectChancePercent(Set.of("BLAZE_BELT")));
        assertEquals(20, EquipmentPassiveMath.reflectChancePercent(Set.of("VANQUISHED_BLAZE_BELT")));
        assertEquals(20, EquipmentPassiveMath.reflectChancePercent(Set.of("BLAZE_BELT", "VANQUISHED_BLAZE_BELT")));
        assertEquals(0, EquipmentPassiveMath.reflectChancePercent(Set.of("SCOVILLE_BELT")));
    }

    @Test
    void counterBonusGrowsPerThousandAndCapsAtTen() {
        assertEquals(0, EquipmentPassiveMath.counterBonus(999));
        assertEquals(1, EquipmentPassiveMath.counterBonus(1_000));
        assertEquals(9, EquipmentPassiveMath.counterBonus(9_999));
        assertEquals(10, EquipmentPassiveMath.counterBonus(10_000));
        assertEquals(10, EquipmentPassiveMath.counterBonus(500_000));
    }

    @Test
    void scovilleDefenseCapsAtOneThousand() {
        assertEquals(0, EquipmentPassiveMath.scovilleDefense(0));
        assertEquals(150, EquipmentPassiveMath.scovilleDefense(3));
        assertEquals(1_000, EquipmentPassiveMath.scovilleDefense(20));
        assertEquals(1_000, EquipmentPassiveMath.scovilleDefense(50));
    }

    @Test
    void deliriumAddsEightPercentPerStrength() {
        assertEquals(1, EquipmentPassiveMath.deliriumMultiplier(0), 1e-9);
        assertEquals(1.08, EquipmentPassiveMath.deliriumMultiplier(1), 1e-9);
        assertEquals(1 + 0.08 * 250, EquipmentPassiveMath.deliriumMultiplier(250), 1e-9);
    }

    @Test
    void scourgeMultipliesFireOneHundredAndOneFoldAndBanksTwentyPercent() {
        assertEquals(101, EquipmentPassiveMath.scourgeFireMultiplier(), 1e-9);
        assertEquals(200, EquipmentPassiveMath.scourgeNextHitBonus(1_000), 1e-9);
        assertEquals(0, EquipmentPassiveMath.scourgeNextHitBonus(0), 1e-9);
    }

    @Test
    void contagionDealsTenPercentOfTheKilledMobsHealth() {
        assertEquals(25_000, EquipmentPassiveMath.contagionDamage(250_000), 1e-9);
    }

    @Test
    void cubeFanaticRoundsFractionsRandomlyAroundTheScaledAmount() {
        assertEquals(0, EquipmentPassiveMath.magmaCreamPercent(Set.of()));
        assertEquals(10, EquipmentPassiveMath.magmaCreamPercent(Set.of("MAGMA_NECKLACE")));
        assertEquals(20, EquipmentPassiveMath.magmaCreamPercent(Set.of("VANQUISHED_MAGMA_NECKLACE")));
        assertEquals(20, EquipmentPassiveMath.scaledAmount(10, 100, new Random(1)));
        assertEquals(10, EquipmentPassiveMath.scaledAmount(10, 0, new Random(1)));
        Random random = new Random(7);
        int extra = 0;
        for (int i = 0; i < 10_000; i++) {
            int amount = EquipmentPassiveMath.scaledAmount(1, 20, random);
            assertTrue(amount == 1 || amount == 2);
            extra += amount - 1;
        }
        assertTrue(extra > 1_800 && extra < 2_200, "expected about 20% extra, got " + extra);
    }
}
