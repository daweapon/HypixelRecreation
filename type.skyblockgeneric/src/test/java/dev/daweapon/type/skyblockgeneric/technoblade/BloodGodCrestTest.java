package dev.daweapon.type.skyblockgeneric.technoblade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodGodCrestTest {
    @Test
    void noKillsGiveNoStrength() {
        assertEquals(0, BloodGodCrest.strengthFor(0));
    }

    @Test
    void strengthIsTheNumberOfDigitsInTheCounter() {
        assertEquals(1, BloodGodCrest.strengthFor(1));
        assertEquals(1, BloodGodCrest.strengthFor(9));
        assertEquals(2, BloodGodCrest.strengthFor(10));
        assertEquals(2, BloodGodCrest.strengthFor(99));
        assertEquals(3, BloodGodCrest.strengthFor(100));
        assertEquals(6, BloodGodCrest.strengthFor(999_999));
    }

    @Test
    void strengthCapsAtSevenFromOneMillionKills() {
        assertEquals(7, BloodGodCrest.strengthFor(1_000_000));
        assertEquals(7, BloodGodCrest.strengthFor(123_456_789_012L));
    }
}
