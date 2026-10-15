package dev.daweapon.type.skyblockgeneric.item.set.sets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorBonusesTest {
    @Test
    void emeraldTankGivesOnePerThreeThousandEmeraldsCappedAt350() {
        assertEquals(0, EmeraldArmorSet.bonus(2_999));
        assertEquals(1, EmeraldArmorSet.bonus(3_000));
        assertEquals(100, EmeraldArmorSet.bonus(300_000));
        assertEquals(350, EmeraldArmorSet.bonus(1_050_000));
        assertEquals(350, EmeraldArmorSet.bonus(50_000_000));
        assertEquals(0, EmeraldArmorSet.bonus(-5));
    }

    @Test
    void cactusDeflectReboundsAThirdOfTheDamage() {
        assertEquals(33, CactusArmorSet.rebound(100), 1e-9);
        assertEquals(0, CactusArmorSet.rebound(0), 1e-9);
        assertEquals(0, CactusArmorSet.rebound(-10), 1e-9);
    }
}
