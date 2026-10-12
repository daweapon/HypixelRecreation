package net.swofty.type.skyblockgeneric.item.armor;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.sets.FinalDestinationDarkness;
import net.swofty.type.skyblockgeneric.item.set.sets.SnorkelingArmorSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorAbilityMathTest {
    @Test
    void tieredTablesScaleWithWornPieces() {
        assertEquals(0, ArmorAbilityMath.tier(ArmorAbilityMath.DEPTH_CHAMPION_REDUCTION, 0), 1e-9);
        assertEquals(3, ArmorAbilityMath.tier(ArmorAbilityMath.DEPTH_CHAMPION_REDUCTION, 1), 1e-9);
        assertEquals(10, ArmorAbilityMath.tier(ArmorAbilityMath.DEPTH_CHAMPION_REDUCTION, 4), 1e-9);
        assertEquals(40, ArmorAbilityMath.tier(ArmorAbilityMath.SWAMP_SOLDIER_BONUS, 4), 1e-9);
        assertEquals(6, ArmorAbilityMath.tier(ArmorAbilityMath.DEEPNESS_WITHIN_HEALTH_PER_LEVEL, 1), 1e-9);
    }

    @Test
    void percentHelpersAdjustDamage() {
        assertEquals(90, ArmorAbilityMath.reducedByPercent(100, 10), 1e-9);
        assertEquals(140, ArmorAbilityMath.boostedByPercent(100, 40), 1e-9);
    }

    @Test
    void mithrilProtectionCapsDamageAtFortyPercentOfHealth() {
        assertEquals(400, ArmorAbilityMath.mithrilProtection(5000, 1000), 1e-9);
        assertEquals(300, ArmorAbilityMath.mithrilProtection(300, 1000), 1e-9);
        assertTrue(ArmorAbilityMath.mithrilProtectionTriggers(401, 1000));
        assertFalse(ArmorAbilityMath.mithrilProtectionTriggers(400, 1000));
    }

    @Test
    void extraDefenseLowersDamageByTheDefenseFormula() {
        assertEquals(100.0 / 150.0, ArmorAbilityMath.extraDefenseMultiplier(0, 50), 1e-9);
        assertEquals(1, ArmorAbilityMath.extraDefenseMultiplier(200, 0), 1e-9);
    }

    @Test
    void octodexterityTriggersOnEveryFourthStrike() {
        assertFalse(ArmorAbilityMath.isOctodexterityStrike(3));
        assertTrue(ArmorAbilityMath.isOctodexterityStrike(4));
        assertTrue(ArmorAbilityMath.isOctodexterityStrike(8));
        assertFalse(ArmorAbilityMath.isOctodexterityStrike(0));
    }

    @Test
    void batPersonMultipliesAtNightAndDuringTheFestival() {
        assertEquals(1, ArmorAbilityMath.batPersonMultiplier(false, false), 1e-9);
        assertEquals(2, ArmorAbilityMath.batPersonMultiplier(false, true), 1e-9);
        assertEquals(3, ArmorAbilityMath.batPersonMultiplier(true, true), 1e-9);
        assertTrue(ArmorAbilityMath.isNight(14000));
        assertFalse(ArmorAbilityMath.isNight(6000));
    }

    @Test
    void obsidianGivesOneSpeedPerTwentyPieces() {
        assertEquals(0, ArmorAbilityMath.obsidianSpeed(19), 1e-9);
        assertEquals(3, ArmorAbilityMath.obsidianSpeed(64), 1e-9);
    }

    @Test
    void mastiffConvertsDefenseIntoCritDamageAndCritDamageIntoHealth() {
        assertEquals(150, ArmorAbilityMath.mastiffCritDamage(300), 1e-9);
        assertEquals(6, ArmorAbilityMath.mastiffHealthPercent(150), 1e-9);
        assertEquals(40, ArmorAbilityMath.mastiffHealthPercent(5000), 1e-9);
    }

    @Test
    void absoluteUnitCapsDefenseAndAddsCritDamage() {
        ItemStatistics base = ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, 500D)
                .withBase(ItemStatistic.CRITICAL_DAMAGE, 50D).withBase(ItemStatistic.HEALTH, 1000D).build();
        ItemStatistics result = ArmorAbilityEffects.absoluteUnit(base);
        assertEquals(300, result.getOverall(ItemStatistic.DEFENSE), 1e-9);
        assertEquals(200, result.getOverall(ItemStatistic.CRITICAL_DAMAGE), 1e-9);
        assertEquals(1080, result.getOverall(ItemStatistic.HEALTH), 1e-9);
    }

    @Test
    void cloverPenaltyReducesEveryStatExceptMagicFind() {
        ItemStatistics base = ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, 100D)
                .withBase(ItemStatistic.MAGIC_FIND, 50D).build();
        ItemStatistics result = ArmorAbilityEffects.cloverPenalty(base);
        assertEquals(95, result.getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(50, result.getOverall(ItemStatistic.MAGIC_FIND), 1e-9);
    }

    @Test
    void racingHelmetRaisesTheSpeedCapByOneHundred() {
        assertEquals(480, ArmorAbilityMath.raisedSpeedCap(480, 400, 100), 1e-9);
        assertEquals(500, ArmorAbilityMath.raisedSpeedCap(700, 400, 100), 1e-9);
    }

    @Test
    void werewolfStacksCapAtTen() {
        assertEquals(10, ArmorAbilityMath.howlStacks(14));
        assertEquals(0, ArmorAbilityMath.howlStacks(-1));
    }

    @Test
    void battalionStacksCapAtFive() {
        assertEquals(5, ArmorAbilityMath.battalionStacks(9));
        assertEquals(2, ArmorAbilityMath.battalionStacks(2));
    }

    @Test
    void longTubaScalesFromTwoPieces() {
        assertEquals(0, SnorkelingArmorSet.respiration(1), 1e-9);
        assertEquals(2, SnorkelingArmorSet.respiration(2), 1e-9);
        assertEquals(5, SnorkelingArmorSet.respiration(3), 1e-9);
        assertEquals(10, SnorkelingArmorSet.respiration(4), 1e-9);
    }

    @Test
    void vivaciousDarknessChargesOnlyWhileSneakingInCombatWithSoulflow() {
        assertTrue(FinalDestinationDarkness.shouldCharge(true, true, false, 2));
        assertFalse(FinalDestinationDarkness.shouldCharge(true, true, false, 1));
        assertFalse(FinalDestinationDarkness.shouldCharge(true, true, true, 10));
        assertFalse(FinalDestinationDarkness.shouldCharge(false, true, false, 10));
        assertTrue(ArmorAbilityState.darknessActive(true, true, true));
        assertFalse(ArmorAbilityState.darknessActive(false, true, true));
        assertTrue(ArmorAbilityState.inCombat(1_000, 5_000));
        assertFalse(ArmorAbilityState.inCombat(1_000, 7_000));
    }

    @Test
    void sneakAbilitiesRespectCooldowns() {
        assertFalse(SneakAbilities.offCooldown(1_000, 3_000, SneakAbilities.GOLEM_HAT_COOLDOWN_MILLIS));
        assertTrue(SneakAbilities.offCooldown(1_000, 6_000, SneakAbilities.GOLEM_HAT_COOLDOWN_MILLIS));
    }

    @Test
    void arachnesFaithfulScalesFromTwoPiecesUpToEight() {
        assertEquals(0, ArmorAbilityMath.arachnesFaithful(1), 1e-9);
        assertEquals(5, ArmorAbilityMath.arachnesFaithful(2), 1e-9);
        assertEquals(35, ArmorAbilityMath.arachnesFaithful(5), 1e-9);
        assertEquals(100, ArmorAbilityMath.arachnesFaithful(8), 1e-9);
        assertEquals(100, ArmorAbilityMath.arachnesFaithful(12), 1e-9);
    }

    @Test
    void radioactiveGivesCritDamagePerTenStrengthUpToAThousand() {
        assertEquals(25, ArmorAbilityMath.radioactiveCritDamage(259), 1e-9);
        assertEquals(100, ArmorAbilityMath.radioactiveCritDamage(4000), 1e-9);
    }
}
