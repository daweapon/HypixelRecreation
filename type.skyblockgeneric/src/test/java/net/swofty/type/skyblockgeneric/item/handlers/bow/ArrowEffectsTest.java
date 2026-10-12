package net.swofty.type.skyblockgeneric.item.handlers.bow;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowEffectsTest {
    @Test
    void armorshredIgnoresFivePercentOfDefense() {
        ItemStatistics enemy = ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, 200.0).build();
        assertEquals(190.0, ArrowEffects.enemyStatistics(ArrowEffect.ARMORSHRED, enemy).getOverall(ItemStatistic.DEFENSE), 1e-9);
        assertSame(enemy, ArrowEffects.enemyStatistics(ArrowEffect.ICY, enemy));
        assertSame(enemy, ArrowEffects.enemyStatistics(null, enemy));
    }

    @Test
    void nansorbReplacesPiercingReductionWithSkewer() {
        assertEquals(0.25, ArrowEffects.pierceFactor(null, 1, 0.25));
        assertEquals(1.0, ArrowEffects.pierceFactor(ArrowEffect.NANSORB, 0, 1));
        assertEquals(1.075, ArrowEffects.pierceFactor(ArrowEffect.NANSORB, 1, 0.25), 1e-9);
        assertEquals(Math.pow(1.075, 4), ArrowEffects.pierceFactor(ArrowEffect.NANSORB, 4, 0.25), 1e-9);
        assertEquals(0, ArrowEffects.pierceFactor(ArrowEffect.NANSORB, 5, 0.25));
    }

    @Test
    void bouncesLoseFifteenPercentEach() {
        assertEquals(1.0, ArrowEffects.bounceFactor(0));
        assertEquals(0.85, ArrowEffects.bounceFactor(1), 1e-9);
        assertEquals(0.85 * 0.85, ArrowEffects.bounceFactor(2), 1e-9);
    }

    @Test
    void glueHasASixSecondCooldownPerVictim() {
        UUID victim = UUID.randomUUID();
        assertTrue(ArrowEffects.tryGlue(victim, 1000));
        assertFalse(ArrowEffects.tryGlue(victim, 1000 + ArrowEffects.GLUE_COOLDOWN_MILLIS - 1));
        assertTrue(ArrowEffects.tryGlue(victim, 1000 + ArrowEffects.GLUE_COOLDOWN_MILLIS));
        assertTrue(ArrowEffects.tryGlue(UUID.randomUUID(), 1000));
    }

    @Test
    void explosionSplitsDamageAndHurtsShooterInRadius() {
        assertEquals(50, ArrowEffects.explosionShare(100, 2));
        assertEquals(0, ArrowEffects.explosionShare(100, 0));
        assertTrue(ArrowEffects.explosionHurtsShooter(2.5));
        assertFalse(ArrowEffects.explosionHurtsShooter(2.6));
    }
}
