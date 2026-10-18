package dev.daweapon.type.skyblockgeneric.enchantment;

import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentCayenne;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentFatalTempo;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentFireProtection;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentFlame;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentHabaneroTactics;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentInferno;
import dev.daweapon.type.skyblockgeneric.item.set.sets.RekindledEmberSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FireEnchantmentMathTest {
    @Test
    void fatalTempoStacksToTheCapAndExpiresAfterThreeSeconds() {
        double bonus = 0;
        for (int hit = 0; hit < 20; hit++) bonus = EnchantmentFatalTempo.nextBonus(bonus, 100, 5);
        assertEquals(200, bonus);
        assertEquals(60, EnchantmentFatalTempo.nextBonus(EnchantmentFatalTempo.nextBonus(0, 100, 3), 100, 3));
        assertEquals(50, EnchantmentFatalTempo.nextBonus(200, 3001, 5));
        assertEquals(0, EnchantmentFatalTempo.activeBonus(100, 3001));
        assertEquals(100, EnchantmentFatalTempo.activeBonus(100, 3000));
    }

    @Test
    void infernoTriggersEveryTenthHitAndScalesPerLevel() {
        assertFalse(EnchantmentInferno.triggers(9));
        assertTrue(EnchantmentInferno.triggers(10));
        assertTrue(EnchantmentInferno.triggers(20));
        assertEquals(1.25, EnchantmentInferno.damageMultiplier(1));
        assertEquals(2.25, EnchantmentInferno.damageMultiplier(5));
    }

    @Test
    void cayenneScalesWithTheDigitsOfAccessoryPower() {
        assertEquals(4, EnchantmentCayenne.digits(1234));
        assertEquals(0, EnchantmentCayenne.digits(0));
        assertEquals(0.8, EnchantmentCayenne.health(4));
        assertEquals(1, EnchantmentCayenne.health(5));
        assertEquals(0.4, EnchantmentCayenne.trueDefense(4));
        assertEquals(0.5, EnchantmentCayenne.trueDefense(5));
    }

    @Test
    void fireProtectionGivesTwoTrueDefensePerLevelAndUsesTheWikiCosts() {
        assertEquals(14, EnchantmentFireProtection.trueDefense(7));
        assertEquals(200, EnchantmentFireProtection.COSTS[6]);
        assertEquals(100, EnchantmentFireProtection.COSTS[5]);
    }

    @Test
    void flameMatchesTheWikiNumbers() {
        assertEquals(3.5, EnchantmentFlame.DURATION_SECONDS[0]);
        assertEquals(0.06, EnchantmentFlame.DAMAGE_PERCENTAGES[1]);
    }

    @Test
    void habaneroTacticsValuesComeFromTheLevel() {
        assertEquals(20, EnchantmentHabaneroTactics.damageBonusPercent(4));
        assertEquals(25, EnchantmentHabaneroTactics.damageBonusPercent(5));
        assertEquals(2.5, EnchantmentHabaneroTactics.combatWisdom(4));
        assertEquals(5, EnchantmentHabaneroTactics.combatWisdom(5));
    }

    @Test
    void rekindleAddsFlatAndPerSecondBurningDamageWithACap() {
        assertEquals(3, RekindledEmberSet.multiplier(0, 0));
        assertEquals(3 + 0.012 * 10, RekindledEmberSet.multiplier(0, 10), 1e-9);
        assertEquals(3.5, RekindledEmberSet.multiplier(0, 1000), 1e-9);
        assertEquals(7 + 1.0, RekindledEmberSet.multiplier(2, 20) , 1e-9);
        assertEquals(9, RekindledEmberSet.multiplier(2, 1000), 1e-9);
    }
}
