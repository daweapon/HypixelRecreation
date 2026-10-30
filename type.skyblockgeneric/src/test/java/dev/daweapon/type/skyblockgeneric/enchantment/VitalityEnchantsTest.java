package dev.daweapon.type.skyblockgeneric.enchantment;

import dev.daweapon.type.skyblockgeneric.enchantment.impl.VitalityEnchants;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VitalityEnchantsTest {
    @Test
    void percentsMatchTheWikiTables() {
        assertEquals(2, VitalityEnchants.strongPercent(1));
        assertEquals(20, VitalityEnchants.strongPercent(10));
        assertEquals(40, VitalityEnchants.hardenedPercent(10));
        assertEquals(10, VitalityEnchants.vivaciousPercent(10));
        assertEquals(0.55, VitalityEnchants.vampiricHealing(1), 1e-9);
        assertEquals(1, VitalityEnchants.vampiricHealing(10), 1e-9);
    }

    @Test
    void strongTenReachesTheCapAtSeventyFiveVitality() {
        assertEquals(15, VitalityEnchants.gainedForTest(Map.of(10, 1), 75, VitalityEnchants::strongPercent), 1e-9);
    }
}
