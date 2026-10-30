package dev.daweapon.type.skyblockgeneric.healing;

import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentHabaneroTactics;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealingMathTest {
    @Test
    void vitalityPoolIsAtLeastTheBaseHundred() {
        assertEquals(100, HealingMath.maxVitality(0), 1e-9);
        assertEquals(250, HealingMath.maxVitality(250), 1e-9);
    }

    @Test
    void regenerationIsFivePercentOfTheStatPerSecondAndClamps() {
        assertEquals(5, HealingMath.regenerated(0, 100), 1e-9);
        assertEquals(10, HealingMath.regenerated(0, 200), 1e-9);
        assertEquals(100, HealingMath.regenerated(98, 100), 1e-9);
    }

    @Test
    void setBonusAndHabaneroStackAdditively() {
        assertEquals(1, HealingMath.multiplier(false, 0), 1e-9);
        assertEquals(1.5, HealingMath.multiplier(true, 0), 1e-9);
        assertEquals(1.5 + 0.075 * 4, HealingMath.multiplier(true, 30), 1e-9);
        assertEquals(60 * 1.5, HealingMath.tickHeal(60, 1.5), 1e-9);
    }

    @Test
    void setBonusNeedsThreeSecondsOfWear() {
        assertFalse(HealingMath.setBonusActive(true, 10_000, 12_999));
        assertTrue(HealingMath.setBonusActive(true, 10_000, 13_000));
        assertFalse(HealingMath.setBonusActive(false, 10_000, 20_000));
        assertFalse(HealingMath.setBonusActive(true, 0, 20_000));
    }

    @Test
    void habaneroGivesSixOrSevenAndAHalfPercentPerPiece() {
        assertEquals(6, EnchantmentHabaneroTactics.healBonusPercent(4), 1e-9);
        assertEquals(7.5, EnchantmentHabaneroTactics.healBonusPercent(5), 1e-9);
        assertEquals(0, EnchantmentHabaneroTactics.healBonusPercent(3), 1e-9);
        assertEquals(6 * 2 + 7.5, EnchantmentHabaneroTactics.wandHealBonusPercent(Map.of(4, 2, 5, 1)), 1e-9);
    }
}
