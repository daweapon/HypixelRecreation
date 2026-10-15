package dev.daweapon.pvp.feature.attack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttackValuesTest {
    @Test
    void pipelineCarriesEveryFieldAndAddsFlagsStepByStep() {
        AttackValues.PreCritical pre = new AttackValues.PreCritical(7f, 2f, 0.9, true, true, 1, 2);
        AttackValues.PreSweeping withCrit = pre.withCritical(true);
        AttackValues.PreSounds withSweep = withCrit.withSweeping(true);

        assertEquals(7f, withSweep.damage());
        assertEquals(2f, withSweep.magicalDamage());
        assertEquals(0.9, withSweep.cooldownProgress());
        assertTrue(withSweep.strong());
        assertTrue(withSweep.sprint());
        assertTrue(withSweep.critical());
        assertTrue(withSweep.sweeping());
        assertEquals(1, withSweep.knockback());
        assertEquals(2, withSweep.fireAspect());
    }

    @Test
    void criticalAndSweepingAreIndependentFlags() {
        AttackValues.PreSounds values = new AttackValues.PreCritical(1f, 0f, 1.0, false, false, 0, 0)
                .withCritical(false).withSweeping(true);
        assertFalse(values.critical());
        assertTrue(values.sweeping());
    }

    @Test
    void vanillaCriticalMultipliesDamageByOnePointFive() {
        VanillaCriticalFeature feature = new VanillaCriticalFeature(new dev.daweapon.pvp.feature.config.FeatureConfiguration());
        assertEquals(15f, feature.applyToDamage(10f), 1e-6);
        assertEquals(0f, feature.applyToDamage(0f), 1e-6);
    }
}
