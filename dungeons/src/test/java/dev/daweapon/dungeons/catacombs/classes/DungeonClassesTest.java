package dev.daweapon.dungeons.catacombs.classes;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DungeonClassesTest {
    private static final DungeonClassRegistry REGISTRY = DungeonClassRegistry.defaults();

    @Test
    void playedClassEarnsFullShareAndTeammateClassesAQuarter() {
        Map<DungeonClassType, Long> earned = DungeonClassXp.earned(DungeonClassType.MAGE,
                List.of(DungeonClassType.TANK, DungeonClassType.TANK, DungeonClassType.HEALER), 80);
        assertEquals(80L, earned.get(DungeonClassType.MAGE));
        assertEquals(20L, earned.get(DungeonClassType.TANK));
        assertEquals(20L, earned.get(DungeonClassType.HEALER));
        assertFalse(earned.containsKey(DungeonClassType.BERSERK));
        assertFalse(earned.containsKey(DungeonClassType.ARCHER));
    }

    @Test
    void duplicateOfOwnClassGivesNoExtraShare() {
        Map<DungeonClassType, Long> earned = DungeonClassXp.earned(DungeonClassType.ARCHER, List.of(DungeonClassType.ARCHER), 40);
        assertEquals(Map.of(DungeonClassType.ARCHER, 40L), earned);
        assertTrue(DungeonClassXp.earned(DungeonClassType.ARCHER, List.of(), 0).isEmpty());
    }

    @Test
    void classLevelsUseTheCatacombsTable() {
        assertEquals(0, DungeonClassXp.level(49));
        assertEquals(1, DungeonClassXp.level(50));
        assertEquals(5, DungeonClassXp.level(625));
        assertEquals(50, DungeonClassXp.level(569_809_640L));
        assertEquals(50, DungeonClassXp.level(Long.MAX_VALUE));
        assertEquals(569_809_640L, DungeonClassXp.capped(Long.MAX_VALUE));
        assertEquals(200, DungeonClassXp.skyBlockXp(0, 50));
        assertEquals(1_000, 5 * DungeonClassXp.skyBlockXp(0, DungeonClassXp.MAXIMUM_LEVEL));
    }

    @Test
    void loneClassGetsDoubleStrength() {
        UUID mage = UUID.randomUUID();
        UUID tankOne = UUID.randomUUID();
        UUID tankTwo = UUID.randomUUID();
        DungeonClassParty party = new DungeonClassParty(Map.of(mage, DungeonClassType.MAGE,
                tankOne, DungeonClassType.TANK, tankTwo, DungeonClassType.TANK));
        assertEquals(2, party.strength(mage));
        assertEquals(1, party.strength(tankOne));
        assertEquals(1, party.strength(UUID.randomUUID()));
        assertEquals(List.of(DungeonClassType.TANK, DungeonClassType.TANK).size(), party.teammatesOf(mage).size());
        assertEquals(2, party.counts().get(DungeonClassType.TANK));
    }

    @Test
    void wikiCooldowns() {
        assertEquals(2, cooldown(DungeonClassType.HEALER, DungeonClassAbilityType.ORB, 0));
        assertEquals(120, cooldown(DungeonClassType.HEALER, DungeonClassAbilityType.ULTIMATE, 0));
        assertEquals(30, cooldown(DungeonClassType.MAGE, DungeonClassAbilityType.ORB, 0));
        assertEquals(500, cooldown(DungeonClassType.MAGE, DungeonClassAbilityType.ULTIMATE, 0));
        assertEquals(10, cooldown(DungeonClassType.BERSERK, DungeonClassAbilityType.ORB, 0));
        assertEquals(60, cooldown(DungeonClassType.BERSERK, DungeonClassAbilityType.ULTIMATE, 0));
        assertEquals(40, cooldown(DungeonClassType.ARCHER, DungeonClassAbilityType.ORB, 0));
        assertEquals(100, cooldown(DungeonClassType.ARCHER, DungeonClassAbilityType.ULTIMATE, 0));
        assertEquals(15, cooldown(DungeonClassType.TANK, DungeonClassAbilityType.ORB, 0));
        assertEquals(150, cooldown(DungeonClassType.TANK, DungeonClassAbilityType.ULTIMATE, 0));
        assertEquals(20, cooldown(DungeonClassType.BERSERK, DungeonClassAbilityType.GHOST, 0));
        assertEquals(10, cooldown(DungeonClassType.BERSERK, DungeonClassAbilityType.GHOST, 1));
        assertEquals(10, cooldown(DungeonClassType.ARCHER, DungeonClassAbilityType.GHOST, 0));
        assertEquals(10, cooldown(DungeonClassType.ARCHER, DungeonClassAbilityType.GHOST, 1));
        assertEquals(20, cooldown(DungeonClassType.MAGE, DungeonClassAbilityType.GHOST, 0));
        assertEquals(15, cooldown(DungeonClassType.MAGE, DungeonClassAbilityType.GHOST, 1));
        assertEquals(20, cooldown(DungeonClassType.TANK, DungeonClassAbilityType.GHOST, 0));
        assertEquals(20, cooldown(DungeonClassType.TANK, DungeonClassAbilityType.GHOST, 1));
        assertEquals(20, cooldown(DungeonClassType.HEALER, DungeonClassAbilityType.GHOST, 0));
        assertEquals(50, cooldown(DungeonClassType.HEALER, DungeonClassAbilityType.GHOST, 1));
    }

    @Test
    void levelScaledCooldowns() {
        DungeonClassAbility explosive = REGISTRY.ability(DungeonClassType.ARCHER, DungeonClassAbilityType.ORB, 0);
        assertEquals(40, DungeonClassScaling.cooldownSeconds(explosive, DungeonClassType.ARCHER, 0));
        assertEquals(34, DungeonClassScaling.cooldownSeconds(explosive, DungeonClassType.ARCHER, 15));
        assertEquals(20, DungeonClassScaling.cooldownSeconds(explosive, DungeonClassType.ARCHER, 50));

        DungeonClassAbility sheep = REGISTRY.ability(DungeonClassType.MAGE, DungeonClassAbilityType.ORB, 0);
        assertEquals(22.5, DungeonClassScaling.cooldownSeconds(sheep, DungeonClassType.MAGE, 0), 1e-9);
        assertEquals(15, DungeonClassScaling.cooldownSeconds(sheep, DungeonClassType.MAGE, 50), 1e-9);
        DungeonClassAbility fireball = REGISTRY.ability(DungeonClassType.MAGE, DungeonClassAbilityType.GHOST, 1);
        assertEquals(15, DungeonClassScaling.cooldownSeconds(fireball, DungeonClassType.MAGE, 50), 1e-9);
    }

    @Test
    void cooldownTracker() {
        AbilityCooldowns cooldowns = new AbilityCooldowns();
        UUID member = UUID.randomUUID();
        assertTrue(cooldowns.tryUse(member, "WISH", 120, 1_000));
        assertFalse(cooldowns.tryUse(member, "WISH", 120, 60_000));
        assertEquals(61_000, cooldowns.remainingMillis(member, "WISH", 60_000));
        assertEquals(61, AbilityCooldowns.displaySeconds(61_000));
        cooldowns.reduce(member, "WISH", 1_000);
        assertTrue(cooldowns.tryUse(member, "WISH", 120, 120_000));
        assertTrue(cooldowns.tryUse(member, "HEALING_CIRCLE", 2, 120_000));
        cooldowns.reset(member);
        assertEquals(0, cooldowns.remainingMillis(member, "WISH", 120_000));
    }

    @Test
    void levelFiftyTotalsMatchTheWiki() {
        assertEquals(100, DungeonClassScaling.renewPercent(50, 1));
        assertEquals(2, DungeonClassScaling.healingAuraPercent(50, 1), 1e-9);
        assertEquals(70, DungeonClassScaling.wishShieldPercent(50, 1));
        assertEquals(35, DungeonClassScaling.ghostHealingPotionPercent(50, 1));
        assertEquals(200, DungeonClassScaling.ghostHealingPotionFlat(50, 1));
        assertEquals(50, DungeonClassScaling.selfReviveSeconds(50));
        assertEquals(500, DungeonClassScaling.mageIntelligence(50, 1));
        assertEquals(15, DungeonClassScaling.mageAbilityDamage(50, 1));
        assertEquals(50, DungeonClassScaling.mageCooldownReductionPercent(50));
        assertEquals(77.5, DungeonClassScaling.berserkMeleePercent(50, 1), 1e-9);
        assertEquals(34, DungeonClassScaling.berserkSpeed(50, 1), 1e-9);
        assertEquals(50, DungeonClassScaling.buffPotionStrength(50, 1));
        assertEquals(100, DungeonClassScaling.doubleshotChancePercent(50, 1));
        assertEquals(9, DungeonClassScaling.rapidFireSeconds(50));
        assertEquals(101, DungeonClassScaling.tankDefense(50, 1));
        assertEquals(15.1, DungeonClassScaling.tankVitality(50, 1), 1e-9);
        assertEquals(35, DungeonClassScaling.tankDefensePercent(50, 1), 1e-9);
        assertEquals(700, DungeonClassScaling.absorptionPotion(50, 1));
        assertEquals(1_420_000, DungeonClassScaling.seismicWaveDamage(50, 0, 1), 1e-6);
        assertEquals(20_000 * 1.2, DungeonClassScaling.seismicWaveDamage(0, 100, 1), 1e-6);
    }

    @Test
    void loneClassDoublesPositiveBonuses() {
        assertEquals(50, DungeonClassScaling.tankDefensePercent(0, DungeonClassParty.LONE_CLASS_MULTIPLIER), 1e-9);
        assertEquals(500, DungeonClassScaling.mageIntelligence(0, DungeonClassParty.LONE_CLASS_MULTIPLIER));
        assertEquals(100, DungeonClassScaling.doubleshotChancePercent(10, DungeonClassParty.LONE_CLASS_MULTIPLIER));
    }

    @Test
    void interpolatedDamageGrowsFromFirstToLastStep() {
        assertEquals(100, DungeonClassScaling.guidedSheepDamage(0, 1), 1e-9);
        assertEquals(200, DungeonClassScaling.guidedSheepDamage(1, 1), 1e-9);
        double fifty = DungeonClassScaling.guidedSheepDamage(50, 1);
        double fortyNine = DungeonClassScaling.guidedSheepDamage(49, 1);
        assertEquals(2_000, fifty - fortyNine, 1e-6);
        assertEquals(0.8 * fifty, DungeonClassScaling.fireballDamage(50, 1), 1e-6);
    }

    private static int cooldown(DungeonClassType type, DungeonClassAbilityType abilityType, int index) {
        return REGISTRY.ability(type, abilityType, index).cooldownSeconds();
    }
}
