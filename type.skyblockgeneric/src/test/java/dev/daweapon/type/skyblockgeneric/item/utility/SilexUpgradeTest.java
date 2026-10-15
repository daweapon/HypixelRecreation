package dev.daweapon.type.skyblockgeneric.item.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SilexUpgradeTest {
    @Test
    void appliesFromEfficiencyFiveUpToButNotPastTen() {
        assertFalse(SilexUpgrade.canUpgrade(0));
        assertFalse(SilexUpgrade.canUpgrade(4));
        assertTrue(SilexUpgrade.canUpgrade(5));
        assertTrue(SilexUpgrade.canUpgrade(9));
        assertFalse(SilexUpgrade.canUpgrade(10));
    }
}
