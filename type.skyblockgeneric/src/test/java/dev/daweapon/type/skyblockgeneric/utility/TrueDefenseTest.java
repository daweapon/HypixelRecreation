package dev.daweapon.type.skyblockgeneric.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrueDefenseTest {
    @Test
    void reductionMatchesTheWikiFormula() {
        assertEquals(1, MobAttackService.trueDefenseMultiplier(0), 1e-9);
        assertEquals(0.5, MobAttackService.trueDefenseMultiplier(100), 1e-9);
        assertEquals(100D / 150D, MobAttackService.trueDefenseMultiplier(50), 1e-9);
        assertEquals(1, MobAttackService.trueDefenseMultiplier(-20), 1e-9);
    }
}
